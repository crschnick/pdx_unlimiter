package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.installation.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.Installation;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Eu4ImageLoader {

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());
    }

    private static Map<String, Image> IMAGES = new HashMap<>();

    public static Image loadImage(Path p) throws IOException {
        if (IMAGES.containsKey(p.toString())) {
            return IMAGES.get(p.toString());
        }

        File file = p.toFile();
        BufferedImage image = ImageIO.read(file);
        WritableImage w = new WritableImage(image.getWidth(), image.getHeight());
        w = SwingFXUtils.toFXImage(image, w);
        IMAGES.put(p.toString(), w);
        return w;
    }

    public static ImageView loadInterfaceImage(String name)  {
        Path p = Installation.EU4.get().getPath().resolve("gfx/interface/" + name);
        Image i = null;
        try {
            i = loadImage(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageView v = new ImageView(i);
        return v;
    }

    public static ImageView loadDisasterImage(String name)  {
        Path p = Installation.EU4.get().getPath().resolve("gfx/interface/disasters/" + name + ".dds");
        Image i = null;
        try {
            i = loadImage(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageView v = new ImageView(i);
        v.setViewport(new Rectangle2D(44, 0, 44, 44));
        return v;
    }

    public static ImageView loadFlagImage(String name, int size) {
        Path p = Installation.EU4.get().getPath().resolve("gfx/flags/" + name + ".tga");
        Image i = null;
        try {
            i = loadImage(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageView v = new ImageView(i);
        //v.setViewport(new Rectangle2D(0, 0, 44, 44));
        v.setFitWidth(size);
        v.setFitHeight(size);
        return v;
    }
}
