package com.crschnick.pdx_unlimiter.app.installation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;

public class WindowsRegistry {

    /**
     *
     * @param location path in the registry
     * @param key registry key
     * @return registry value or null if not found
     */
    public static final Optional<String> readRegistry(String location, String key){
        try {
            // Run reg query, then read output with StreamReader (internal class)
            Process process = Runtime.getRuntime().exec("reg query " +
                    '"'+ location + "\" /v " + key);

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            // Output has the following format:
            // \n<Version information>\n\n<key>\t<registry type>\t<value>
            if(output.contains("\t")){
                String[] parsed = output.split("\t");
                return Optional.of(parsed[parsed.length-1]);
            }

            if (output.contains("    ")) {
                String[] parsed = output.split("    ");
                return Optional.of(parsed[parsed.length-1].substring(0, parsed[parsed.length-1].length() - 4));
            }

            return Optional.empty();
        }
        catch (Exception e) {
            return Optional.empty();
        }

    }

    static class StreamReader extends Thread {
        private InputStream is;
        private StringWriter sw= new StringWriter();

        public StreamReader(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            }
            catch (IOException e) {
                System.err.println(e.toString());
            }
        }

        public String getResult() {
            return sw.toString();
        }
    }
}