package net.mika.mikamods.bootstrap;

import net.fabricmc.tinyremapper.*;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;

public class Remapper {
    public static void remapIfNeeded(Path input, Path output, Collection<Path> classPath) throws Exception {
        if (Files.exists(output)) return;

        System.out.println("Remapping jar on first launch... This may take few seconds");

        InputStream yarnIn = Main.class.getResourceAsStream("/yarn.tiny");
        BufferedReader yarn = new BufferedReader(new InputStreamReader(yarnIn));
        IMappingProvider yarnMappings = TinyUtils.createTinyMappingProvider(yarn, "official", "named");

        remapJar(input, output, yarnMappings, classPath);
        copyResources(input, output);

        System.out.println("Original jar: " + input + ", remapped jar: " + output);
    }

    private static void remapJar(
            Path input,
            Path output,
            IMappingProvider mappings,
            Collection<Path> classpath
    ) throws Exception {

        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(mappings)
                .renameInvalidLocals(true)
                .keepInputData(true)
                .build();

        OutputConsumerPath outputConsumer =
                new OutputConsumerPath.Builder(output).build();

        try {
            // IMPORTANT: classpath first
            for (Path cp : classpath) {
                remapper.readClassPath(cp);
            }

            remapper.readInputs(input);
            remapper.apply(outputConsumer);

        } finally {
            // proper manual cleanup (Java 8 style)
            try {
                outputConsumer.close();
            } catch (Exception ignored) {}

            try {
                remapper.finish();
            } catch (Exception ignored) {}
        }
    }

    public static Path findMinecraftJarFromClasspath() {
        String classpath = System.getProperty("java.class.path");

        for (String entry : classpath.split(File.pathSeparator)) {
            File file = new File(entry);

            if (!file.exists())
                continue;

            String name = file.getName().toLowerCase();

            // Minecraft jar usually contains version in name
            if (name.contains("1.16.5") && name.endsWith(".jar") && !name.contains("mikamods")) {
                return Paths.get(file.getAbsolutePath());
            }

            // fallback heuristic
            if (name.contains("minecraft") && name.endsWith(".jar") && !name.contains("mikamods")) {
                return Paths.get(file.getAbsolutePath());
            }
        }

        throw new RuntimeException("Minecraft jar not found in classpath");
    }

    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];

        int n;
        while ((n = in.read(data)) != -1) {
            buffer.write(data, 0, n);
        }

        return buffer.toByteArray();
    }

    public static void copyResources(Path fromJar, Path toJar) throws Exception {
        Map<String, byte[]> resources = new HashMap<>();

        try (JarFile jarFile = new JarFile(fromJar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.isDirectory()) continue;
                if (entry.getName().endsWith(".class")) continue;
                if (entry.getName().toLowerCase().endsWith(".rsa")) continue;
                if (entry.getName().toLowerCase().endsWith(".sf")) continue;

                try (InputStream in = jarFile.getInputStream(entry)) {
                    resources.put(entry.getName(), readFully(in));
                }
            }
        }

        // Now append resources into output jar (SAFE way)
        Path tempJar = Files.createTempFile("merge", ".jar");

        try (
                JarOutputStream out = new JarOutputStream(Files.newOutputStream(tempJar));
                JarFile existing = new JarFile(toJar.toFile())
        ) {
            // 1. copy already remapped jar content
            Enumeration<JarEntry> existingEntries = existing.entries();

            while (existingEntries.hasMoreElements()) {
                JarEntry e = existingEntries.nextElement();

                try (InputStream in = existing.getInputStream(e)) {

                    out.putNextEntry(new JarEntry(e.getName()));
                    byte[] data = readFully(in);
                    out.write(data);
                    out.closeEntry();
                }
            }

            // 2. add missing resources from original jar
            for (Map.Entry<String, byte[]> e : resources.entrySet()) {
                out.putNextEntry(new JarEntry(e.getKey()));
                out.write(e.getValue());
                out.closeEntry();
            }
        }

        Files.move(tempJar, toJar, StandardCopyOption.REPLACE_EXISTING);
    }
}
