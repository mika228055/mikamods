package net.mika.mikamods.loader;

import java.io.File;
import java.util.List;

public class ModContainer {
    public final String id;
    public final String version;
    public final String mainClass;
    public final List<String> mixins;
    public final File file;
    public final List<Dependency> dependencies;
    public final List<String> jars;

    public ModContainer(String id, String version, String mainClass, List<String> mixins, File file, List<Dependency> dependencies, List<String> jars) {
        this.id = id;
        this.version = version;
        this.mainClass = mainClass;
        this.mixins = mixins;
        this.file = file;
        this.dependencies = dependencies;
        this.jars = jars;
    }

    public static class Dependency {
        public final String id;
        public final String version;
        public final boolean required;

        public Dependency(String id, String version, boolean required) {
            this.id = id;
            this.version = version;
            this.required = required;
        }
    }
}
