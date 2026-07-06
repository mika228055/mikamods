package net.mika.mikamods.resource;

import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;

import java.util.function.Consumer;

public class ModResourcePackProvider implements ResourcePackProvider {

    @Override
    public void register(Consumer<ResourcePackProfile> consumer,
                         ResourcePackProfile.Factory factory) {

        ResourcePackProfile profile = ResourcePackProfile.of(
                "mikamods",
                true,
                ModResourceRegistry::getPack,
                factory,
                ResourcePackProfile.InsertionPosition.field_14281,
                ResourcePackSource.field_25348);

        if (consumer != null) consumer.accept(profile);
    }
}