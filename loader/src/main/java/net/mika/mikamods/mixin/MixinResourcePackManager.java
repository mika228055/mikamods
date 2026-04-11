package net.mika.mikamods.mixin;

import com.google.common.collect.ImmutableSet;
import net.mika.mikamods.loader.ModContainer;
import net.mika.mikamods.loader.ModLoader;
import net.mika.mikamods.loader.ModResourcePackProvider;
import net.mika.mikamods.util.LoggerUtil;
import net.minecraft.resource.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ResourcePackManager.class)
public class MixinResourcePackManager {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableSet;copyOf([Ljava/lang/Object;)Lcom/google/common/collect/ImmutableSet;"
            )
    )
    private <E> ImmutableSet<E> onCreateProviders(Object[] original) {
        List<E> list = new ArrayList<>();

        // copy original elements
        for (Object obj : original) {
            list.add((E) obj);
        }

        // add your provider
        list.add((E) new ModResourcePackProvider());

        return ImmutableSet.copyOf(list);
    }
}