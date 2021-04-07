import com.crschnick.pdx_unlimiter.app.core.SavegameTool;
import com.crschnick.pdx_unlimiter.melter.MelterSavegameTool;

module com.crschnick.pdx_unlimiter.melter {
    requires com.crschnick.pdx_unlimiter.app;
    requires com.crschnick.pdx_unlimiter.gui_utils;
    requires com.crschnick.pdx_unlimiter.core;

    requires javafx.controls;
    requires org.apache.commons.io;

    provides SavegameTool with MelterSavegameTool;
}