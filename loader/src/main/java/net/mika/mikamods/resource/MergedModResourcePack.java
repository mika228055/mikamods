package net.mika.mikamods.resource;

import net.mika.mikamods.util.LoggerUtil;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public class MergedModResourcePack implements ResourcePack {

    private final List<ModResourcePack> packs;
    private final String name = "MikaMods";

    public MergedModResourcePack(List<ModResourcePack> packs) {
        this.packs = packs;
    }

    // -----------------------------
    // READ (highest priority first)
    // -----------------------------
    @Override
    public InputStream open(ResourceType type, Identifier id) throws IOException {
        for (int i = packs.size() - 1; i >= 0; i--) {
            ModResourcePack pack = packs.get(i);

            if (pack.contains(type, id)) {
                return pack.open(type, id);
            }
        }
        throw new FileNotFoundException(id.toString());
    }

    // -----------------------------
    // FIND RESOURCES (IMPORTANT FIX)
    // -----------------------------
    @Override
    public Collection<Identifier> findResources(ResourceType type,
                                                String namespace,
                                                String prefix,
                                                int maxDepth,
                                                Predicate<String> filter) {
        List<Identifier> result = new ArrayList<>();

        for (ModResourcePack pack : packs) {

            Collection<Identifier> found =
                    pack.findResources(type, namespace, prefix, maxDepth, filter);

            if (found != null) {
                result.addAll(found);
            }
        }

        return result;
    }

    // -----------------------------
    // CONTAINS
    // -----------------------------
    @Override
    public boolean contains(ResourceType type, Identifier id) {
        for (int i = packs.size() - 1; i >= 0; i--) {
            if (packs.get(i).contains(type, id)) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------
    // NAMESPACES
    // -----------------------------
    @Override
    public Set<String> getNamespaces(ResourceType type) {
        Set<String> namespaces = new HashSet<>();

        for (ModResourcePack pack : packs) {
            Set<String> ns = pack.getNamespaces(type);
            if (ns != null) namespaces.addAll(ns);
        }

        return namespaces;
    }

    // -----------------------------
    // ROOT FILES
    // -----------------------------
    @Override
    public InputStream openRoot(String fileName) throws IOException {

        if ("pack.mcmeta".equals(fileName)) {
            return MergedModResourcePack.class.getResourceAsStream("/pack.mcmeta");
        } else if ("pack.png".equals(fileName)) {
            return MergedModResourcePack.class.getResourceAsStream("/icon.png");
        }

        return null; // Fabric-style fallback
    }

    // -----------------------------
    // METADATA
    // -----------------------------
    @Nullable
    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> reader) throws IOException {
        try (InputStream stream = openRoot("pack.mcmeta")) {
            if (stream == null) return null;
            return AbstractFileResourcePack.parseMetadata(reader, stream);
        } catch (Exception e) {
            return null;
        }
    }

    // -----------------------------
    // LIFECYCLE
    // -----------------------------
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void close() {
        for (ModResourcePack pack : packs) {
            try {
                pack.close();
            } catch (Exception ignored) {}
        }
    }
}