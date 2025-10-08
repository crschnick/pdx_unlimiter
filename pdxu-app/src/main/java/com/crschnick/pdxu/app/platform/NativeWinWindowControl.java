package com.crschnick.pdxu.app.platform;

import javafx.stage.Window;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

@Getter
@EqualsAndHashCode
public class NativeWinWindowControl {

    private final WinDef.HWND windowHandle;

    public NativeWinWindowControl(Window stage) {
        this.windowHandle = byWindow(stage);
    }

    @SneakyThrows
    public static WinDef.HWND byWindow(Window window) {
        Method tkStageGetter = Window.class.getDeclaredMethod("getPeer");
        tkStageGetter.setAccessible(true);
        Object tkStage = tkStageGetter.invoke(window);
        Method getPlatformWindow = tkStage.getClass().getDeclaredMethod("getPlatformWindow");
        getPlatformWindow.setAccessible(true);
        Object platformWindow = getPlatformWindow.invoke(tkStage);
        Method getNativeHandle = platformWindow.getClass().getMethod("getNativeHandle");
        getNativeHandle.setAccessible(true);
        Object nativeHandle = getNativeHandle.invoke(platformWindow);
        var hwnd = new WinDef.HWND(new Pointer((long) nativeHandle));
        return hwnd;
    }

    public boolean setWindowAttribute(int attribute, boolean attributeValue) {
        var r = Dwm.INSTANCE.DwmSetWindowAttribute(
                windowHandle, attribute, new WinDef.BOOLByReference(new WinDef.BOOL(attributeValue)), WinDef.BOOL.SIZE);
        return r.longValue() == 0;
    }

    public boolean setWindowBackdrop(DwmSystemBackDropType backdrop) {
        var r = Dwm.INSTANCE.DwmSetWindowAttribute(
                windowHandle,
                DmwaWindowAttribute.DWMWA_SYSTEMBACKDROP_TYPE.get(),
                new WinDef.DWORDByReference(new WinDef.DWORD(backdrop.get())),
                WinDef.DWORD.SIZE);
        return r.longValue() == 0;
    }

    public enum DmwaWindowAttribute {
        DWMWA_USE_IMMERSIVE_DARK_MODE(20),
        DWMWA_SYSTEMBACKDROP_TYPE(38);

        private final int value;

        DmwaWindowAttribute(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }
    }

    @SuppressWarnings("unused")
    public enum DwmSystemBackDropType {
        // DWMSBT_NONE
        NONE(1),
        // DWMSBT_MAINWINDOW
        MICA(2),
        // DWMSBT_TRANSIENTWINDOW
        ACRYLIC(3),
        // DWMSBT_TABBEDWINDOW
        MICA_ALT(4);

        private final int value;

        DwmSystemBackDropType(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }
    }

    public interface Dwm extends Library {

        Dwm INSTANCE = Native.load("dwmapi", Dwm.class);

        WinNT.HRESULT DwmSetWindowAttribute(
                WinDef.HWND hwnd, int dwAttribute, PointerType pvAttribute, int cbAttribute);
    }
}
