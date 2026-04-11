package net.mika.mikamods.loader;

import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ZipResourcePack;

import java.util.function.Consumer;

public class ModResourcePackProvider implements ResourcePackProvider {
    @Override
    public void register(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory) {
        for (ModContainer mod : ModLoader.getMods()) {
            if (mod.file == null) continue;
            try {
                ResourcePackProfile profile = ResourcePackProfile.of(
                        mod.id,
                        true,
                        () -> new ZipResourcePack(mod.file),
                        factory,
                        ResourcePackProfile.InsertionPosition.TOP,
                        ResourcePackSource.PACK_SOURCE_BUILTIN
                );

                if (profile != null) {
                    profileAdder.accept(profile);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}