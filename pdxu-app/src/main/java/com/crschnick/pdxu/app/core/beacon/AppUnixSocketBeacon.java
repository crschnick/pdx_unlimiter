package com.crschnick.pdxu.app.core.beacon;

import com.crschnick.pdxu.app.core.AppDataLock;
import com.crschnick.pdxu.app.core.AppNames;
import com.crschnick.pdxu.app.core.AppSystemInfo;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.JacksonMapper;
import com.crschnick.pdxu.app.util.ThreadHelper;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;

public class AppUnixSocketBeacon extends AppBeacon {

    private static final int MAX_BUFFER_SIZE = 1024;

    private ServerSocketChannel serverSocket;

    public boolean isExistingBeaconRunning() {
        return serverSocket == null;
    }

    public void sendRequest(AppBeaconMessage message) {
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(getSocketAddress());

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.clear();
            buffer.put(JacksonMapper.getDefault().writeValueAsString(message).getBytes());
            buffer.flip();

            channel.write(buffer);
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable(e).handle();
        }
    }

    private static UnixDomainSocketAddress getSocketAddress() {
        var file =
                AppSystemInfo.ofCurrent().getTemp().resolve(AppNames.ofCurrent().getKebapName() + ".sock");
        return UnixDomainSocketAddress.of(file);
    }

    private void createSocket() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverChannel.bind(getSocketAddress());
        serverSocket = serverChannel;
    }

    private void runThread() {
        ThreadHelper.createPlatformThread("beacon", false, () -> {
                    while (true) {
                        try {
                            SocketChannel channel = serverSocket.accept();
                            ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
                            int bytesRead = channel.read(buffer);
                            if (bytesRead < 0) {
                                break;
                            }

                            byte[] bytes = new byte[bytesRead];
                            buffer.flip();
                            buffer.get(bytes);
                            var message = new String(bytes);

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
                        } catch (Exception e) {
                            ErrorEventFactory.fromThrowable(e).omit().handle();
                        }
                    }

                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        ErrorEventFactory.fromThrowable(e).handle();
                    }
                    serverSocket = null;
                })
                .start();
    }

    protected void stop() {
        if (serverSocket == null) {
            return;
        }

        sendRequest(new AppBeaconMessage.ExitRequest());
    }

    private boolean checkSocketExists() {
        var socket = getSocketAddress();
        return Files.exists(socket.getPath());
    }

    protected void start() throws IOException {
        if (checkSocketExists()) {
            if (AppDataLock.hasLock()) {
                if (Files.exists(getSocketAddress().getPath())) {
                    Files.delete(getSocketAddress().getPath());
                }
            } else {
                return;
            }
        }

        createSocket();
        runThread();
    }
}
