package com.cleanroommc.bogosorter.core;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@IFMLLoadingPlugin.Name("BogoSorter-Core")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class BogoSorterCore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return "com.cleanroommc.bogosorter.core.BogoSorterTransformer";
    }

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixin.bogosorter.json");
    }

}
