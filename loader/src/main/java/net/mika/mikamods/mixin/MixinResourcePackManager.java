package net.mika.mikamods.mixin;

import net.mika.mikamods.resource.ModResourcePackProvider;
import net.mika.mikamods.util.LoggerUtil;
import net.minecraft.resource.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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