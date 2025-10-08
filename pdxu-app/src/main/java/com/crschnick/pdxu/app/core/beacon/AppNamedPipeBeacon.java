package com.crschnick.pdxu.app.core.beacon;

import com.crschnick.pdxu.app.core.AppNames;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.JacksonMapper;
import com.crschnick.pdxu.app.util.ThreadHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class AppNamedPipeBeacon extends AppBeacon {

    private static final int MAX_BUFFER_SIZE = 1024;

    private WinNT.HANDLE serverPipeHandle;

    public boolean isExistingBeaconRunning() {
        return serverPipeHandle == null;
    }

    @SneakyThrows
    public void sendRequest(AppBeaconMessage message) {
        var pipeName = getNamedPipeLocation().toString();

        var waitSuccess = Kernel32.INSTANCE.WaitNamedPipe(pipeName, (int) TimeUnit.SECONDS.toMillis(15L));
        if (!waitSuccess) {
            return;
        }

        WinNT.HANDLE hPipe = Kernel32.INSTANCE.CreateFile(
                pipeName, WinNT.GENERIC_READ | WinNT.GENERIC_WRITE, 0, null, WinNT.OPEN_EXISTING, 0, null);
        if (WinBase.INVALID_HANDLE_VALUE.equals(hPipe)) {
            return;
        }

        IntByReference lpMode = new IntByReference(WinBase.PIPE_READMODE_MESSAGE);
        Kernel32.INSTANCE.SetNamedPipeHandleState(hPipe, lpMode, null, null);

        String expMessage = JacksonMapper.getDefault().writeValueAsString(message);
        byte[] expData = expMessage.getBytes();
        IntByReference lpNumberOfBytesWritten = new IntByReference(0);
        Kernel32.INSTANCE.WriteFile(hPipe, expData, expData.length, lpNumberOfBytesWritten, null);

        Kernel32.INSTANCE.DisconnectNamedPipe(hPipe);
        Kernel32.INSTANCE.CloseHandle(hPipe);
    }

    private static Path getNamedPipeLocation() {
        var pipeName = "\\\\.\\pipe\\" + AppNames.ofCurrent().getKebapName();
        return Path.of(pipeName);
    }

    private void createServerNamedPipe() throws IOException {
        var pipeName = getNamedPipeLocation().toString();
        WinNT.HANDLE hNamedPipe = Kernel32.INSTANCE.CreateNamedPipe(
                pipeName,
                WinBase.PIPE_ACCESS_DUPLEX,
                WinBase.PIPE_TYPE_MESSAGE | WinBase.PIPE_READMODE_MESSAGE | WinBase.PIPE_WAIT,
                1,
                Byte.MAX_VALUE,
                Byte.MAX_VALUE,
                30000,
                null);
        if (WinBase.INVALID_HANDLE_VALUE.equals(hNamedPipe)) {
            throw new IOException(
                    "Unable to create named pipe at " + pipeName + ". " + Kernel32Util.getLastErrorMessage());
        }

        serverPipeHandle = hNamedPipe;
    }

    private void runThread() {
        ThreadHelper.createPlatformThread("beacon", false, () -> {
                    while (true) {
                        var connected = Kernel32.INSTANCE.ConnectNamedPipe(serverPipeHandle, null);
                        if (!connected && Kernel32.INSTANCE.GetLastError() != Kernel32.ERROR_PIPE_CONNECTED) {
                            break;
                        }

                        byte[] readBuffer = new byte[MAX_BUFFER_SIZE];
                        IntByReference lpNumberOfBytesRead = new IntByReference(0);
                        Kernel32.INSTANCE.ReadFile(
                                serverPipeHandle, readBuffer, readBuffer.length, lpNumberOfBytesRead, null);

                        Kernel32.INSTANCE.DisconnectNamedPipe(serverPipeHandle);

                        try {
                            var message =
                                    new String(readBuffer, 0, lpNumberOfBytesRead.getValue()).replaceAll("\r\n", "\n");
                            var parsed = JacksonMapper.getDefault().readValue(message, AppBeaconMessage.class);

                            if (parsed instanceof AppBeaconMessage.ExitRequest) {
                                break;
                            }

                            TrackEvent.withTrace("Received message")
                                    .tag("message", message)
                                    .tag("parsed", parsed)
                                    .handle();

                            if (AppOperationMode.isInStartup() || AppOperationMode.isInShutdown()) {
                                continue;
                            }

                            if (parsed != null) {
                                parsed.handle();
                            }
                        } catch (JsonProcessingException e) {
                            ErrorEventFactory.fromThrowable(e).omit().handle();
                        }
                    }

                    Kernel32.INSTANCE.CloseHandle(serverPipeHandle);
                    serverPipeHandle = null;
                })
                .start();
    }

    protected void stop() {
        if (serverPipeHandle == null) {
            return;
        }

        sendRequest(new AppBeaconMessage.ExitRequest());
    }

    private boolean checkNamedPipeExists() {
        var data = new WinBase.WIN32_FIND_DATA();
        var r = Kernel32.INSTANCE.FindFirstFile(getNamedPipeLocation().toString(), data.getPointer());
        if (!WinBase.INVALID_HANDLE_VALUE.equals(r)) {
            Kernel32.INSTANCE.FindClose(r);
            return true;
        }

        return false;
    }

    protected void start() throws IOException {
        if (checkNamedPipeExists()) {
            return;
        }

        createServerNamedPipe();
        runThread();
    }
}
