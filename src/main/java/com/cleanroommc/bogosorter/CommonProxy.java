package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.common.OreDictHelper;
import com.cleanroommc.bogosorter.common.config.Serializer;
import com.cleanroommc.bogosorter.common.lock.LockSlotCapability;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import com.cleanroommc.bogosorter.common.sort.DefaultRules;
import com.cleanroommc.bogosorter.compat.DefaultCompat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CommonProxy {

    public void preInit() {
        NetworkHandler.init();
        OreDictHelper.init();
        BogoSortAPI.INSTANCE.remapSortRule("is_block", "block_type");
        DefaultRules.init(BogoSortAPI.INSTANCE);
        DefaultCompat.init(BogoSortAPI.INSTANCE);
        Serializer.loadConfig();
        MinecraftForge.EVENT_BUS.register(RefillHandler.class);
        CapabilityManager.INSTANCE.register(LockSlotCapability.class, new LockSlotCapability.Storage(), LockSlotCapability.Default::new);
    }

    public void init() {}

    public void postInit() {}
}
