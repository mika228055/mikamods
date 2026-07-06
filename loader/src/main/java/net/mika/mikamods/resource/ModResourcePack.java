package net.mika.mikamods.resource;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public interface ModResourcePack {

    InputStream open(ResourceType type, Identifier id) throws IOException;

    boolean contains(ResourceType type, Identifier id);

    Set<String> getNamespaces(ResourceType type);

    Collection<Identifier> findResources(
            ResourceType type,
            String namespace,
            String prefix,
            int maxDepth,
            Predicate<String> pathFilter
    );

    void close();
}