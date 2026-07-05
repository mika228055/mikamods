package net.mika.mikamods.client;

import net.mika.mikamods.loader.ModContainer;
import net.mika.mikamods.loader.ModLoader;
import net.mika.mikamods.util.LoggerUtil;
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
                        ResourcePackProfile.InsertionPosition.field_14281,
                        ResourcePackSource.field_25348
                );

                if (profile != null) {
                    profileAdder.accept(profile);
                }

                LoggerUtil.info("Added resource pack for mod: " + mod.id);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}