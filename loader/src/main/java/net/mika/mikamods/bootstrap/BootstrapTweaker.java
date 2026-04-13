package net.mika.mikamods.bootstrap;

import net.mika.mikamods.util.Constants;
import net.mika.mikamods.util.LoggerUtil;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

public class BootstrapTweaker implements ITweaker {
    private Map<String, String> args;
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        LoggerUtil.info("Launching MikaMods v" + Constants.MODLOADER_VERSION + " with LaunchWrapper");

        Thread.currentThread().setContextClassLoader(Launch.classLoader);
        this.args = (Map<String, String>) Launch.blackboard.get("launchArgs");

        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.mikamods.json");

        if (this.args == null) {
            this.args = new HashMap<>();
            Launch.blackboard.put("launchArgs", this.args);
        }

        if (!this.args.containsKey("--version")) {
            this.args.put("--version", profile != null ? profile : "MikaMods");
        }

        if (!this.args.containsKey("--gameDir") && gameDir != null) {
            this.args.put("--gameDir", gameDir.getAbsolutePath());
        }

        if (!this.args.containsKey("--assetsDir") && assetsDir != null) {
            this.args.put("--assetsDir", assetsDir.getAbsolutePath());
        }
        if (!this.args.containsKey("--accessToken")) {
            this.args.put("--accessToken", "MikaMods");
        }

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.startsWith("--")) {
                this.args.put(arg, args.get(i + 1));
            }
        }

        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("named");
        MixinEnvironment.getDefaultEnvironment().setOption(MixinEnvironment.Option.DEBUG_VERBOSE, true);

        LoggerUtil.info("Tweaker initialized");
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.addClassLoaderExclusion("org.spongepowered.asm.");
        classLoader.addClassLoaderExclusion("org.objectweb.asm.");
        classLoader.addClassLoaderExclusion("net.mika.mikamods.bootstrap.");
        classLoader.addClassLoaderExclusion("net.mika.mikamods.api.");

        try {
            InputStream is = classLoader.getResourceAsStream("mikamods.accesswidener");

            if (is == null) {
                LoggerUtil.error("No mikamods.accesswidener found");
                throw new RuntimeException();
            }

            Class<?> awClass = classLoader.loadClass(
                    "net.mika.mikamods.access.AccessWidener"
            );

            Class<?> readerClass = classLoader.loadClass(
                    "net.mika.mikamods.access.AccessWidenerReader"
            );

            java.lang.reflect.Method readMethod =
                    readerClass.getMethod("read", InputStream.class);

            Object awInstance = readMethod.invoke(null, is);

            Class<?> holderClass = classLoader.loadClass(
                    "net.mika.mikamods.access.AccessWidenerHolder"
            );

            java.lang.reflect.Field field = holderClass.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            field.set(null, awInstance);

            classLoader.registerTransformer("net.mika.mikamods.access.AccessWidenerTransformer");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String gameDirStr = args.get("--gameDir");
        Path gameDir = Paths.get(gameDirStr);
        Path modsDir = Paths.get(gameDir.toString(), "mods");
        try {
            Class<?> modLoaderClass = classLoader.loadClass("net.mika.mikamods.loader.ModLoader");
            Method init = modLoaderClass.getMethod("init", LaunchClassLoader.class, Path.class);
            init.invoke(null, classLoader, modsDir);

            Method loadMods = modLoaderClass.getMethod("loadMods");
            loadMods.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        List<String> launchArgs = new ArrayList<>();
        for (Map.Entry<String, String> arg : this.args.entrySet()) {
            launchArgs.add(arg.getKey());
            launchArgs.add(arg.getValue());
        }
        return launchArgs.toArray(new String[launchArgs.size()]);
    }
}
