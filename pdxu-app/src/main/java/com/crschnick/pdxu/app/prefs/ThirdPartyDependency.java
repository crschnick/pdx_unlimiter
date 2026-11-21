package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.core.AppResources;

import lombok.Value;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

@Value
public class ThirdPartyDependency {

    private static List<ThirdPartyDependency> ALL;

    String name;
    String version;
    String licenseName;
    String licenseText;
    String link;

    private static List<ThirdPartyDependency> load() {
        var all = new ArrayList<ThirdPartyDependency>();
        AppResources.with(AppResources.MAIN_MODULE, "third-party", path -> {
            if (!Files.exists(path)) {
                return;
            }

            try (var list = Files.list(path)) {
                for (var p : list.filter(p -> p.getFileName().toString().endsWith(".properties"))
                        .sorted(Comparator.comparing(Path::toString))
                        .toList()) {
                    var props = new Properties();
                    try (var in = Files.newInputStream(p)) {
                        props.load(in);
                    }

                    var textFile = p.resolveSibling(
                            FilenameUtils.getBaseName(p.getFileName().toString()) + ".license");
                    var text = Files.readString(textFile);
                    all.add(new ThirdPartyDependency(
                            props.getProperty("name"),
                            props.getProperty("version"),
                            props.getProperty("license"),
                            text,
                            props.getProperty("link")));
                }
            }
        });
        return List.copyOf(all);
    }

    public static synchronized List<ThirdPartyDependency> getAll() {
        if (ALL == null) {
            ALL = load();
        }
        return ALL;
    }
}
