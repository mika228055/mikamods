package net.mika.mikamods.loader;

import com.google.gson.*;
import net.mika.mikamods.util.Constants;
import net.mika.mikamods.util.LoggerUtil;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.mixin.Mixins;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.*;

public class ModLoader {

    private static final List<ModContainer> mods = new ArrayList<>();
    private static final Set<String> loadedJars = new HashSet<>();

    private static LaunchClassLoader classLoader;
    private static Path modsPath;

    public static void init(LaunchClassLoader loader, Path modsDir) {
        classLoader = loader;
        modsPath = modsDir;
    }

    public static List<ModContainer> getMods() {
        return mods;
    }

    public static void loadMods() {
        mods.clear();
        loadedJars.clear();

        // base mods
        mods.add(new ModContainer("minecraft", "1.16.5", null, new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>()));
        mods.add(new ModContainer("mikamods", Constants.MODLOADER_VERSION, null, new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>()));
        mods.add(new ModContainer("java", getJavaMajorVersion(), null, new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>()));

        File modsDir = modsPath.toFile();

        if (!modsDir.exists()) {
            modsDir.mkdirs();
            LoggerUtil.info("Created mods folder");
            return;
        }

        File[] files = modsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) return;

        for (File file : files) {
            loadModFromJar(file);
        }

        System.out.println("=== Mods Loaded ===");
        for (ModContainer mod : mods) {
            System.out.println("- " + mod.id + " " + mod.version);
        }

        // Mixins
        for (ModContainer mod : mods) {
            for (String mixin : mod.mixins) {
                LoggerUtil.info("Adding mixin: " + mixin);
                Mixins.addConfiguration(mixin);
            }
        }

        // Dependencies
        for (ModContainer mod : mods) {
            for (ModContainer.Dependency dep : mod.dependencies) {

                ModContainer target = findMod(dep.id);

                if (target == null) {
                    if (dep.required) {
                        throw new RuntimeException("Missing dependency: " + dep.id + " for " + mod.id);
                    }
                    continue;
                }

                if (!matches(dep.version, target.version)) {
                    throw new RuntimeException("Version mismatch: " + dep.id);
                }
            }
        }

        // Init mods
        List<ModContainer> sorted = sortMods();

        for (ModContainer mod : sorted) {
            if (mod.mainClass == null) continue;

            try {
                Class<?> clazz = classLoader.loadClass(mod.mainClass);
                Method init = clazz.getMethod("onInitialize");
                init.invoke(null);

                LoggerUtil.info("Initialized mod: " + mod.id);

            } catch (Throwable t) {
                throw new RuntimeException("Failed to init mod: " + mod.id, t);
            }
        }
    }

    // =========================
    // 🔥 Core: load mod jar
    // =========================
    private static void loadModFromJar(File file) {
        try {
            String path = file.getAbsolutePath();

            if (loadedJars.contains(path)) return;
            loadedJars.add(path);

            LoggerUtil.info("Loading jar: " + file.getName());

            classLoader.addURL(file.toURI().toURL());

            JarFile jar = new JarFile(file);

            JarEntry entry = jar.getJarEntry("mika.mod.json");

            // Library jar (no mod json)
            if (entry == null) {
                LoggerUtil.info("Library detected: " + file.getName());
                return;
            }

            JsonObject json = new JsonParser().parse(new InputStreamReader(jar.getInputStream(entry))).getAsJsonObject();

            String id = json.get("id").getAsString();
            String version = json.get("version").getAsString();
            String mainClass = json.has("main") ? json.get("main").getAsString() : null;

            // mixins
            List<String> mixins = new ArrayList<>();
            if (json.has("mixins")) {
                for (JsonElement el : json.getAsJsonArray("mixins")) {
                    mixins.add(el.getAsString());
                }
            }

            // dependencies
            List<ModContainer.Dependency> deps = new ArrayList<>();
            if (json.has("depends")) {
                for (JsonElement el : json.getAsJsonArray("depends")) {
                    JsonObject dep = el.getAsJsonObject();

                    deps.add(new ModContainer.Dependency(
                            dep.get("id").getAsString(),
                            dep.has("version") ? dep.get("version").getAsString() : "*",
                            !dep.has("required") || dep.get("required").getAsBoolean()
                    ));
                }
            }

            // 🔥 jars (jar-in-jar)
            List<String> jars = new ArrayList<>();
            if (json.has("jars")) {
                for (JsonElement el : json.getAsJsonArray("jars")) {
                    jars.add(el.getAsString());
                }
            }

            // 🔥 load embedded jars FIRST
            for (String jarPath : jars) {
                JarEntry nested = jar.getJarEntry(jarPath);

                if (nested == null) {
                    LoggerUtil.warn("Missing embedded jar: " + jarPath);
                    continue;
                }

                File tempJar = extractTempJar(file.getName(), jarPath, jar.getInputStream(nested));

                loadModFromJar(tempJar); // recursive (mod OR lib)
            }

            ModContainer mod = new ModContainer(id, version, mainClass, mixins, file, deps, jars);
            mods.add(mod);

            LoggerUtil.info("Loaded mod: " + id);

        } catch (Exception e) {
            throw new RuntimeException("Failed loading jar: " + file.getName(), e);
        }
    }

    // =========================
    // Extract nested jar
    // =========================
    private static File extractTempJar(String parent, String name, InputStream is) throws IOException {
        File temp = File.createTempFile(parent + "_" + name.replace("/", "_"), ".jar");
        temp.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(temp)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }

        return temp;
    }

    // =========================
    // Utils
    // =========================

    private static ModContainer findMod(String id) {
        for (ModContainer mod : mods) {
            if (mod.id.equals(id)) return mod;
        }
        return null;
    }

    private static boolean matches(String required, String actual) {
        if (required.equals("*")) return true;
        if (required.startsWith(">=")) {
            return compare(actual, required.substring(2)) >= 0;
        }
        return actual.equals(required);
    }

    private static int compare(String v1, String v2) {
        String[] a = v1.split("\\.");
        String[] b = v2.split("\\.");

        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int n1 = i < a.length ? parse(a[i]) : 0;
            int n2 = i < b.length ? parse(b[i]) : 0;

            if (n1 != n2) return Integer.compare(n1, n2);
        }
        return 0;
    }

    private static int parse(String s) {
        StringBuilder num = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) num.append(c);
            else break;
        }
        return num.length() == 0 ? 0 : Integer.parseInt(num.toString());
    }

    private static String getJavaMajorVersion() {
        String v = System.getProperty("java.version");

        if (v.startsWith("1.")) return v.substring(2, 3);

        int dot = v.indexOf(".");
        return dot != -1 ? v.substring(0, dot) : v;
    }

    private static List<ModContainer> sortMods() {
        List<ModContainer> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (ModContainer mod : mods) {
            visit(mod, visited, result);
        }

        return result;
    }

    private static void visit(ModContainer mod, Set<String> visited, List<ModContainer> result) {
        if (visited.contains(mod.id)) return;
        visited.add(mod.id);

        for (ModContainer.Dependency dep : mod.dependencies) {
            ModContainer depMod = findMod(dep.id);
            if (depMod != null) visit(depMod, visited, result);
        }

        result.add(mod);
    }
}