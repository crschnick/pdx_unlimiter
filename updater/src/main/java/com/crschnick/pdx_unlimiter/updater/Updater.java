package com.crschnick.pdx_unlimiter.updater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Updater {

    public static final String PATH = Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "app").toString();

    public static void main(String[] args) {
        try {
            String response = executeGet("https://api.github.com/repos/crschnick/pdx_unlimiter/releases/latest");
            System.out.println("got response: " + response);
            if (response.equals("")) {
                return;
            }
            String toDonwload = getDownloadUrl(response);
            String pathToNewest = downloadNewestVersion(toDonwload);
            deleteOldVersion(PATH);
            unzip(pathToNewest, PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void deleteOldVersion(String path) throws Exception {
        File f = new File(path);
        f.delete();
        f.mkdir();
    }

    private static void unzip(String zipFilePath, String destDir) throws Exception {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        fis = new FileInputStream(zipFilePath);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = zis.getNextEntry();
        while(ze != null){
            String fileName = ze.getName();
            File newFile = new File(destDir + File.separator + fileName);
            System.out.println("Unzipping to "+newFile.getAbsolutePath());
            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            //close this ZipEntry
            zis.closeEntry();
            ze = zis.getNextEntry();
        }
        //close last ZipEntry
        zis.closeEntry();
        zis.close();
        fis.close();
    }

    public static String downloadNewestVersion(String url) throws Exception {
        String file = executeGet(url);
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir, Paths.get(url).getFileName().toString());
        Files.write(path, file.getBytes());
        return path.toString();
    }

    public static String getDownloadUrl(String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        for (JsonNode n : node.get("assets")) {
            if ((SystemUtils.IS_OS_WINDOWS && n.get("name").textValue().contains("windows"))
                    || (SystemUtils.IS_OS_MAC && n.get("name").textValue().contains("mac"))
                    || (SystemUtils.IS_OS_LINUX && n.get("name").textValue().contains("linux"))) {
                return n.get("browser_download_url").textValue();
            }
        }
        throw new FileNotFoundException("Couldn't find download url");
    }


    public static String executeGet(String targetURL) throws Exception {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Got http " + responseCode + " for " + targetURL);
            }

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
