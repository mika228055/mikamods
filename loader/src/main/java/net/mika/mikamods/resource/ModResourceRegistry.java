package net.mika.mikamods.resource;

import net.mika.mikamods.util.LoggerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModResourceRegistry {

    // All loaded mod packs (Zip wrappers)
    private static final List<ModResourcePack> PACKS = new ArrayList<>();

    // Single merged pack instance (Fabric-style)
    private static MergedModResourcePack merged;

    // -----------------------------
    // REGISTER MOD
    // -----------------------------
    public static void registerMod(File jarFile) {
        try {
            PACKS.add(new ZipModResourcePack(jarFile));

            // IMPORTANT: rebuild merged pack
            rebuild();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -----------------------------
    // BUILD MERGED PACK
    // -----------------------------
    private static void rebuild() {
        merged = new MergedModResourcePack(PACKS);
    }

    // -----------------------------
    // GET FINAL PACK (used by Minecraft)
    // -----------------------------
    public static MergedModResourcePack getPack() {
        if (merged == null) {
            rebuild();
        }
        return merged;
    }
}