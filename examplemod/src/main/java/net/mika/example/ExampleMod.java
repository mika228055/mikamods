package net.mika.example;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.mika.mikamods.api.ModInitializer;
import net.minecraft.util.registry.Registry;

public class ExampleMod implements ModInitializer {
    public ExampleMod() {
    }

    public void onInitialize() {
        System.out.println("examplemod initialized");

        Registry.register(Registry.ITEM, new Identifier("examplemod", "example_item"), new Item(new Item.Settings().group(ItemGroup.TOOLS)));

        Block exampleBlock = new Block(Block.Settings.of(Material.STONE).strength(3.0f));

        // Register the block
        Registry.register(Registry.BLOCK, new Identifier("examplemod", "example_block"), exampleBlock);

        // Register the BlockItem (so it appears in inventory)
        Registry.register(Registry.ITEM, new Identifier("examplemod", "example_block"),
                new BlockItem(exampleBlock, new Item.Settings().group(ItemGroup.TOOLS)));
    }
}
