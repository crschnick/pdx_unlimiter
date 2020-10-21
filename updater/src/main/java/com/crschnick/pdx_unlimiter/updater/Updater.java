package com.crschnick.pdx_unlimiter.updater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Updater {

    public static void main(String[] args) {
        Path dir = null;
        try {
            dir = Optional.ofNullable(System.getProperty("pdxu.installDir"))
                    .map(Path::of)
                    .orElse(Path.of(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        initErrorHandler(dir);
        UpdaterGui frame = new UpdaterGui();
        try {
            update(frame,
                    new URL("https://api.github.com/repos/crschnick/pdx_unlimiter/releases/latest"),
                    dir.resolve("app"),
                    dir.resolve("app"),
                    true);
        } catch (Throwable t) {
            Sentry.capture(t);
            t.printStackTrace();
        }


        try {
            update(frame,
                    new URL("https://api.github.com/repos/crschnick/pdxu_achievements/releases/latest"),
                    dir.resolve("achievements"),
                    dir.resolve("achievements"),
                    false);
        } catch (Throwable t) {
            Sentry.capture(t);
            t.printStackTrace();
        }


        try {
            update(frame,
                    new URL("https://api.github.com/repos/crschnick/pdxu_launcher/releases/latest"),
                    dir.resolve("launcher_new"),
                    dir.resolve("launcher"),
                    true);
        } catch (Throwable t) {
            Sentry.capture(t);
            t.printStackTrace();
        }

        try {
            run(dir);
        } catch (IOException e) {
            Sentry.capture(e);
            e.printStackTrace();
        }

        frame.dispose();

    }

    private static void run(Path dir) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(
                List.of("cmd.exe", "/C", dir.resolve("app").resolve("bin").resolve("pdxu.bat").toString()));
        builder.redirectErrorStream(true);
        builder.start();
    }

    private static void initErrorHandler(Path p)  {
        p.resolve("logs").toFile().mkdirs();
        System.setProperty("org.slf4j.simpleLogger.logFile", p.resolve("logs").resolve("updater.log").toString());
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
        Sentry.init();
        LoggerFactory.getLogger(Updater.class).info("Initializing updater at " + p.toString());
    }

    private static void update(UpdaterGui frame, URL url, Path out, Path checkDir, boolean platformSpecific) throws Exception {
        byte[] response = executeGet(url, 0, null);

        Info info = getDownloadInfo(platformSpecific, new String(response));
        if (!requiresUpdate(info, checkDir)) {
            return;
        }

        frame.setVisible(true);
        Path pathToNewest = downloadNewestVersion(info.url, info.size, frame::setProgress);
        deleteOldVersion(out);
        unzip(pathToNewest, out);
        Files.write(out.resolve("update"), info.timestamp.toString().getBytes());
        Files.write(out.resolve("version"), info.version.getBytes());
        frame.setVisible(false);
    }

    private static boolean requiresUpdate(Info info, Path p) {
        Instant i = Instant.MIN;
        Path f = p.resolve("update");
        try {
            i = Instant.parse(Files.readString(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i.compareTo(info.timestamp) < 0;
    }

    public static void deleteOldVersion(Path path) throws Exception {
        File f = path.toFile();
        FileUtils.deleteDirectory(f);
        FileUtils.forceMkdir(f);
    }

    private static void unzip(Path zipFilePath, Path destDir) throws Exception {
        File dir = destDir.toFile();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        FileUtils.forceMkdir(dir);

        ZipFile f = new ZipFile(zipFilePath.toString());
        for (Iterator<? extends ZipEntry> it = f.stream().iterator(); it.hasNext(); ) {
            ZipEntry e = it.next();
            String fileName = e.getName();
            Path p = destDir.resolve(fileName);
            if (e.isDirectory()) {
                FileUtils.forceMkdir(p.toFile());
            } else {
                Files.write(p, f.getInputStream(e).readAllBytes());
            }
        }
        f.close();
    }

    public static Path downloadNewestVersion(URL url, int size, Consumer<Float> c) throws Exception {
        byte[] file = executeGet(url, size, c);
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir, url.getFile());
        FileUtils.forceMkdirParent(path.toFile());
        Files.write(path, file);
        return path;
    }

    public static Info getDownloadInfo(boolean platformSpecific, String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        for (JsonNode n : node.get("assets")) {
            if (!n.get("name").textValue().endsWith(".zip")) {
                continue;
            }

            if (!platformSpecific || (SystemUtils.IS_OS_WINDOWS && n.get("name").textValue().contains("windows"))
                    || (SystemUtils.IS_OS_MAC && n.get("name").textValue().contains("mac"))
                    || (SystemUtils.IS_OS_LINUX && n.get("name").textValue().contains("linux"))) {
                Info i = new Info();
                i.url = new URL(n.get("browser_download_url").textValue());
                i.size = n.get("size").intValue();
                i.timestamp = Instant.parse(n.get("updated_at").textValue());
                i.body = node.get("body").asText();
                i.version = node.get("tag_name").textValue();
                return i;
            }
        }
        throw new IOException("Couldn't find download url");
    }

    public static byte[] executeGet(URL targetURL, int size, Consumer<Float> progress) throws Exception {
        HttpURLConnection connection = null;

        try {
            //Create connection
            connection = (HttpURLConnection) targetURL.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Got http " + responseCode + " for " + targetURL);
            }

            InputStream is = connection.getInputStream();
            if (size == 0) {
                return is.readAllBytes();
            }

            byte[] line;
            int bytes = 0;
            ByteBuffer b = ByteBuffer.allocate(size);
            while ((line = is.readNBytes(1000000)).length > 0) {
                b.put(line);
                bytes += line.length;
                if (progress != null) progress.accept((float) bytes / (float) size);
            }
            return b.array();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static class Info {
        public URL url;
        public int size;
        public Instant timestamp;
        public String body;
        public String version;
    }
}
