package net.mika.mikamods.resource;

import net.minecraft.resource.ZipResourcePack;

import java.io.File;
import net.minecraft.resource.ResourceType;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipModResourcePack extends ZipResourcePack implements ModResourcePack {

    private final ZipFile file;

    public ZipModResourcePack(File file) throws Exception {
        super(file);
        this.file = new ZipFile(file);
    }

    @Override
    public String getName() {
        return "MikaMods";
    }
}