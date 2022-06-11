package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;

@Mod.EventBusSubscriber(modid = BogoSorter.ID, value = Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onGui(GuiScreenEvent.InitGuiEvent.Post event) {
        /*if (event.getGui() instanceof GuiContainer && !(event.getGui() instanceof GuiContainerCreative)) {
            Container container = ((GuiContainer) event.getGui()).inventorySlots;
            NetworkHandler.sendToServer(new CSlotPosUpdate(container));
        }*/
    }

    @SubscribeEvent
    public static void onMouseInput(GuiScreenEvent.MouseInputEvent.Post event) {
        if (Mouse.getEventButton() == 2 && event.getGui() instanceof GuiContainer) {
            Slot slot = ((GuiContainer) event.getGui()).getSlotUnderMouse();
            if (slot == null) return;
            Container container = ((GuiContainer) event.getGui()).inventorySlots;
            boolean player = BogoSortAPI.INSTANCE.isPlayerSlot(container, slot);
            if (!player && !isSortableContainer(event.getGui())) return;

            SortHandler sortHandler = new SortHandler(container, player);
            sortHandler.sort(slot.slotNumber);
        }
    }

    public static boolean isSortableContainer(GuiScreen screen) {
        return screen instanceof GuiContainer && BogoSortAPI.isValidSortable(((GuiContainer) screen).inventorySlots);
    }

    public static <T extends Container & ISortableContainer> T getSortableContainer(GuiScreen screen) {
        return (T) ((GuiContainer) screen).inventorySlots;
    }
}
