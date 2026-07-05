package net.mika.mikamods.mixin;

import com.google.common.collect.ImmutableSet;
import net.mika.mikamods.client.ModResourcePackProvider;
import net.minecraft.resource.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.*;

@Mixin(ResourcePackManager.class)
public class MixinResourcePackManager {
    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableSet;copyOf([Ljava/lang/Object;)Lcom/google/common/collect/ImmutableSet;"
            )
    )
    private Object[] addProviders(Object[] original) {

        List<Object> list = new ArrayList<>(Arrays.asList(original));

        list.add(new ModResourcePackProvider());

        return list.toArray();
    }
}