package com.cleanroommc.bogosorter.common.sort;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

public class ButtonHandler {

    private static final int SORT_ID = 394658246;
    private static final int SETTINGS_ID = 394658247;
    public static final int BUTTON_SIZE = 10;

    public static final UITexture BUTTON_BACKGROUND = UITexture.builder()
            .location(BogoSorter.ID, "gui/base_button")
            .imageSize(18, 18)
            .adaptable(1)
            .build();

    @SubscribeEvent
    public static void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (ClientEventHandler.isSortableContainer(event.getGui()) && !(event.getGui() instanceof GuiScreenWrapper)) {
            Container container = ((GuiContainer) event.getGui()).inventorySlots;
            GuiSortingContext context = GuiSortingContext.getOrCreate(container);
            event.getButtonList().removeIf(guiButton -> guiButton instanceof SortButton);
            for (SlotGroup slotGroup : context.getSlotGroups()) {
                if (slotGroup.canBeSorted() && slotGroup.getPosSetter() != null) {
                    event.getButtonList().add(new SortButton(slotGroup, true));
                    event.getButtonList().add(new SortButton(slotGroup, false));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (ClientEventHandler.isSortableContainer(event.getGui()) && !(event.getGui() instanceof GuiScreenWrapper)) {
            GuiContainer gui = (GuiContainer) event.getGui();
            IGuiContainerAccessor guiAccess = (IGuiContainerAccessor) gui;
            GuiSortingContext context = GuiSortingContext.getOrCreate(gui.inventorySlots);
            ButtonPos buttonPos = new ButtonPos();
            for (SlotGroup slotGroup : context.getSlotGroups()) {
                if (slotGroup.getPosSetter() == null) continue;
                SortButton sortButton = null, settingsButton = null;
                for (GuiButton guiButton : guiAccess.getButtons()) {
                    if (guiButton instanceof SortButton) {
                        SortButton button = (SortButton) guiButton;
                        if (button.slotGroup == slotGroup) {
                            if (button.sort) sortButton = button;
                            else settingsButton = button;
                            if (sortButton != null && settingsButton != null) {
                                break;
                            }
                        }
                    }
                }
                if (sortButton == null || settingsButton == null) continue;
                buttonPos.reset();
                slotGroup.getPosSetter().setButtonPos(slotGroup, buttonPos);
                sortButton.enabled = buttonPos.isEnabled();
                settingsButton.enabled = buttonPos.isEnabled();
                buttonPos.applyPos(guiAccess.getGuiLeft(), guiAccess.getGuiTop(), sortButton, settingsButton);
            }
        }
    }

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (ClientEventHandler.isSortableContainer(event.getGui()) && !(event.getGui() instanceof GuiScreenWrapper)) {
            for (GuiButton guiButton : ((IGuiContainerAccessor) event.getGui()).getButtons()) {
                if (guiButton instanceof SortButton) {
                    ((SortButton) guiButton).drawTooltip(event.getMouseX(), event.getMouseY());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getButton() instanceof SortButton && event.getButton().enabled) {
            SortButton sortButton = (SortButton) event.getButton();
            if (sortButton.sort) {
                ClientEventHandler.sort(event.getGui(), sortButton.slotGroup.getSlots().get(0));
            } else {
                BogoSortAPI.INSTANCE.openConfigGui();
                Interactable.playButtonClickSound();
            }
            event.setCanceled(true);
        }
    }

    public static class SortButton extends GuiButton {

        private final SlotGroup slotGroup;
        private final boolean sort;

        public SortButton(SlotGroup slotGroup, boolean sort) {
            super(sort ? SORT_ID : SETTINGS_ID, 0, 0, BUTTON_SIZE, BUTTON_SIZE, sort ? "z" : "...");
            this.slotGroup = slotGroup;
            this.sort = sort;
        }

        @Override
        public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible && this.enabled) {
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                Color.setGlColor(BogoSorterConfig.buttonColor);
                BUTTON_BACKGROUND.draw(this.x, this.y, this.width, this.height);
                Color.resetGlColor();
                this.mouseDragged(mc, mouseX, mouseY);
                int color = 14737632;

                if (packedFGColour != 0) {
                    color = packedFGColour;
                } else if (!this.enabled) {
                    color = 10526880;
                } else if (this.hovered) {
                    color = 16777120;
                }
                int y = this.y;
                if (!this.sort) y -= 1;
                this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, y, color);
            }
        }

        public void drawTooltip(int mouseX, int mouseY) {
            if (this.enabled && this.hovered) {
                GuiScreen guiScreen = Objects.requireNonNull(Minecraft.getMinecraft().currentScreen);
                GuiUtils.drawHoveringText(Collections.singletonList(I18n.format(this.sort ? "key.sort" : "key.sort_config")), mouseX, mouseY, guiScreen.width, guiScreen.height, 300, Minecraft.getMinecraft().fontRenderer);
            }
        }
    }
}
