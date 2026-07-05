package net.mika.mikamods.bootstrap;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("MikaMods remapper");

        Collection<Path> classPath = new ArrayList<>();

        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            File file = new File(entry);

            String name = file.getName();

            if (!file.exists()) continue;
            if (!name.endsWith(".jar")) continue;

            classPath.add(file.toPath());
        }

        Path remappedJar = Paths.get("mikamods/1.16.5-named.jar");
        Path originalJar = Remapper.findMinecraftJarFromClasspath();

        Remapper.remapIfNeeded(originalJar, remappedJar, classPath);

        Path backUp = Paths.get(originalJar + ".old");
        if (!Files.exists(backUp)) {
            copyFileUnlocked(originalJar, backUp);
            copyFileUnlocked(remappedJar, originalJar);
        }

        Launch.main(args);
    }

    public static void copyFileUnlocked(Path src, Path dst) throws Exception {
        try (InputStream in = Files.newInputStream(src);
             OutputStream out = Files.newOutputStream(dst)) {

            byte[] buffer = new byte[8192];
            int len;

            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
    }
}
