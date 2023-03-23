package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigGui extends ModularScreen {

    public static boolean wasOpened = false;
    public static final UITexture TOGGLE_BUTTON = UITexture.fullImage("bogosorter:gui/toggle_config");
    public static final UITexture ARROW_DOWN_UP = UITexture.fullImage("bogosorter:gui/arrow_down_up");

    private Map<SortRule<ItemStack>, AvailableElement> availableElements;
    private Map<NbtSortRule, AvailableElement> availableElementsNbt;

    public ConfigGui() {
        super(BogoSorter.ID, "config");
    }

    @Override
    public ModularPanel buildUI(GuiContext guiContext) {
        this.availableElements = new Object2ObjectOpenHashMap<>();
        this.availableElementsNbt = new Object2ObjectOpenHashMap<>();
        ModularPanel panel = ModularPanel.defaultPanel(guiContext, 300, 250);

        PagedWidget.Controller controller = new PagedWidget.Controller();

        panel.child(new TextWidget(IKey.lang("bogosort.gui.title"))
                        .left(0.5f)
                        .top(5))
                .child(new Rectangle().setColor(Color.BLACK.bright(7)).asWidget()
                        .left(4)
                        .right(4)
                        .height(1)
                        .top(16))
                .child(new PagedWidget<>()
                        .controller(controller)
                        .left(4).right(4)
                        .top(35).bottom(4)
                        .addPage(createGeneralConfigUI(guiContext))
                        .addPage(createProfilesConfig(guiContext)))
                .child(new Row()
                        .left(4).right(4)
                        .height(16).top(18)
                        .child(new PageButton(0, controller)
                                .size(0.5f, 1f)
                                .background(true, GuiTextures.BUTTON, IKey.lang("bogosort.gui.tab.general.name").color(Color.WHITE.normal).shadow(true))
                                .background(false, GuiTextures.SLOT_DARK, IKey.lang("bogosort.gui.tab.general.name").color(Color.WHITE.normal).shadow(true)))
                        .child(new PageButton(1, controller)
                                .size(0.5f, 1f)
                                .background(true, GuiTextures.BUTTON, IKey.lang("bogosort.gui.tab.profiles.name").color(Color.WHITE.normal).shadow(true))
                                .background(false, GuiTextures.SLOT_DARK, IKey.lang("bogosort.gui.tab.profiles.name").color(Color.WHITE.normal).shadow(true))));
        return panel;
    }

    public IWidget createGeneralConfigUI(GuiContext context) {
        Row row = new Row();
        return new ListWidget<>()
                .left(5).right(5).top(2).bottom(2)
                .child(new Rectangle().setColor(0xFF606060).asWidget()
                        .top(1)
                        .left(32)
                        .size(1, 48))
                .child(new Row()
                        .width(1f).height(14)
                        .margin(0, 2)
                        .child(new CycleButtonWidget()
                                .toggle(() -> PlayerConfig.getClient().enableAutoRefill, val -> PlayerConfig.getClient().enableAutoRefill = val)
                                .texture(TOGGLE_BUTTON)
                                .size(14, 14)
                                .margin(8, 0))
                        .child(IKey.lang("bogosort.gui.enable_refill").asWidget()
                                .height(14)
                                .marginLeft(10)))
                .child(new Row()
                        .width(1f).height(14)
                        .margin(0, 2)
                        .child(new TextFieldWidget()
                                .getterLong(() -> PlayerConfig.getClient().autoRefillDamageThreshold)
                                .setterLong(val -> PlayerConfig.getClient().autoRefillDamageThreshold = (int) val)
                                .setNumbers(1, Short.MAX_VALUE)
                                .setTextAlignment(Alignment.Center)
                                .background(new Rectangle().setColor(0xFFb1b1b1))
                                .size(30, 14))
                        .child(IKey.lang("bogosort.gui.refill_threshold").asWidget()
                                .marginLeft(10)
                                .height(14)))
                .child(row
                        .width(1f).height(14)
                        .margin(0, 2)
                        .child(new CycleButtonWidget()
                                .toggle(HotbarSwap::isEnabled, HotbarSwap::setEnabled)
                                .texture(TOGGLE_BUTTON)
                                .addTooltipLine(IKey.lang("bogosort.gui.hotbar_scrolling.tooltip"))
                                .tooltipShowUpTimer(10)
                                .excludeTooltipArea(row.getArea())
                                .size(14, 14)
                                .margin(8, 0))
                        .child(IKey.lang("bogosort.gui.hotbar_scrolling").asWidget()
                                .marginLeft(10)
                                .height(14)
                                .addTooltipLine(IKey.lang("bogosort.gui.hotbar_scrolling.tooltip"))
                                .tooltipShowUpTimer(10)
                                .excludeTooltipArea(row.getArea())));
    }

    public IWidget createProfilesConfig(GuiContext context) {
        PagedWidget.Controller controller = new PagedWidget.Controller();
        return new ParentWidget<>()
                .width(1f).top(2).bottom(0)
                .child(new Rectangle().setColor(Color.BLACK.bright(7)).asWidget()
                        .top(0)
                        .bottom(4)
                        .width(1)
                        .left(89))
                .child(new ListWidget<>() // Profiles
                        .pos(2, 2)
                        .width(81).bottom(2)
                        .child(new ButtonWidget<>()
                                .width(1f).height(16)
                                .background(GuiTextures.BUTTON, IKey.str("Profile 1").color(Color.WHITE.normal).shadow(true)))
                        .child(IKey.str("Profiles are not yet implemented. They will come in one of the next versions.").asWidget()
                                .top(20).width(81)))
                .child(new Row()
                        .left(92).right(2)
                        .height(16).top(2)
                        .child(new PageButton(0, controller)
                                .size(0.5f, 1f)
                                .background(true, GuiTextures.BUTTON, IKey.lang("bogosort.gui.tab.item_sort_rules.name").color(Color.WHITE.normal).shadow(true))
                                .background(false, GuiTextures.SLOT_DARK, IKey.lang("bogosort.gui.tab.item_sort_rules.name").color(Color.WHITE.normal).shadow(true)))
                        .child(new PageButton(1, controller)
                                .size(0.5f, 1f)
                                .background(true, GuiTextures.BUTTON, IKey.lang("bogosort.gui.tab.nbt_sort_rules.name").color(Color.WHITE.normal).shadow(true))
                                .background(false, GuiTextures.SLOT_DARK, IKey.lang("bogosort.gui.tab.nbt_sort_rules.name").color(Color.WHITE.normal).shadow(true))))
                .child(new PagedWidget<>()
                        .controller(controller)
                        .left(90).right(0)
                        .top(16).bottom(0)
                        .addPage(createItemSortConfigUI(context))
                        .addPage(createNbtSortConfigUI(context)));
    }

    public IWidget createItemSortConfigUI(GuiContext context) {
        List<SortRule<ItemStack>> allValues = BogoSortAPI.INSTANCE.getItemSortRuleList();
        AtomicReference<SortableListWidget<SortRule<ItemStack>, SortListItem<SortRule<ItemStack>>>> ref = new AtomicReference<>(null);
        List<List<AvailableElement>> availableMatrix = Grid.mapToMatrix(2, allValues, (index, value) -> {
            AvailableElement availableElement = new AvailableElement()
                    .background(IKey.lang(value.getNameLangKey()).color(Color.WHITE.normal).shadow(true))
                    .tooltip(tooltip -> tooltip.addLine(IKey.lang(value.getDescriptionLangKey())).showUpTimer(4))
                    .size(80, 14)
                    .onMousePressed(mouseButton1 -> {
                        if (this.availableElements.get(value).available) {
                            ref.get().add(value, -1);
                            this.availableElements.get(value).available = false;
                        }
                        return true;
                    });
            this.availableElements.put(value, availableElement);
            return availableElement;
        });
        for (SortRule<ItemStack> value : allValues) {
            this.availableElements.get(value).available = !BogoSorterConfig.sortRules.contains(value);
        }

        SortableListWidget<SortRule<ItemStack>, SortListItem<SortRule<ItemStack>>> sortableListWidget = SortableListWidget.sortableBuilder(allValues, BogoSorterConfig.sortRules, s -> {
            TextWidget ruleText = IKey.lang(s.getNameLangKey()).asWidget().color(Color.WHITE.normal).shadow(true);
            return new SortListItem<>(s, ruleText
                    .paddingLeft(7)
                    .background(GuiTextures.BUTTON)
                    .tooltip(tooltip -> tooltip.addLine(IKey.lang(s.getDescriptionLangKey())).showUpTimer(10).excludeArea(ruleText.getArea())));
        });
        ref.set(sortableListWidget);
        return new ParentWidget<>()
                .size(1f, 1f)
                .child(sortableListWidget
                        .onRemove(stringItem -> {
                            this.availableElements.get(stringItem.getValue()).available = true;
                        })
                        .onChange(list -> {
                            BogoSorterConfig.sortRules.clear();
                            BogoSorterConfig.sortRules.addAll(list);
                        })
                        .left(7).right(7).top(7).bottom(23))
                .child(new ButtonWidget<>()
                        .bottom(7).size(12, 12).left(0.5f)
                        .background(GuiTextures.BUTTON, GuiTextures.ADD)
                        .onMousePressed(mouseButton -> {
                            if (!isPanelOpen("choose_item_rules")) {
                                ModularPanel panel1 = ModularPanel.defaultPanel(context, 200, 140).name("choose_item_rules");
                                openPanel(panel1
                                        .child(new ButtonWidget<>()
                                                .size(8, 8)
                                                .top(4).right(4)
                                                .background(GuiTextures.BUTTON, GuiTextures.CLOSE)
                                                .onMousePressed(mouseButton1 -> {
                                                    closePanel(panel1);
                                                    return true;
                                                }))
                                        .child(new Grid()
                                                .matrix(availableMatrix)
                                                .scrollable()
                                                .pos(7, 7).right(17).bottom(7)));
                            }
                            return true;
                        }));
    }

    public IWidget createNbtSortConfigUI(GuiContext context) {
        List<NbtSortRule> allValues = BogoSortAPI.INSTANCE.getNbtSortRuleList();
        AtomicReference<SortableListWidget<NbtSortRule, SortListItem<NbtSortRule>>> ref = new AtomicReference<>(null);
        List<List<AvailableElement>> availableMatrix = Grid.mapToMatrix(2, allValues, (index, value) -> {
            AvailableElement availableElement = new AvailableElement()
                    .background(IKey.lang(value.getNameLangKey()).color(Color.WHITE.normal).shadow(true))
                    .tooltip(tooltip -> tooltip.addLine(IKey.lang(value.getDescriptionLangKey())).showUpTimer(4))
                    .size(80, 14)
                    .onMousePressed(mouseButton1 -> {
                        if (this.availableElementsNbt.get(value).available) {
                            ref.get().add(value, -1);
                            this.availableElementsNbt.get(value).available = false;
                        }
                        return true;
                    });
            this.availableElementsNbt.put(value, availableElement);
            return availableElement;
        });
        for (NbtSortRule value : allValues) {
            this.availableElementsNbt.get(value).available = !BogoSorterConfig.nbtSortRules.contains(value);
        }

        SortableListWidget<NbtSortRule, SortListItem<NbtSortRule>> sortableListWidget = SortableListWidget.sortableBuilder(allValues, BogoSorterConfig.nbtSortRules, s -> {
            TextWidget ruleText = IKey.lang(s.getNameLangKey()).asWidget().color(Color.WHITE.normal).shadow(true);
            return new SortListItem<>(s, ruleText
                    .paddingLeft(7)
                    .background(GuiTextures.BUTTON)
                    .tooltip(tooltip -> tooltip.addLine(IKey.lang(s.getDescriptionLangKey())).showUpTimer(10).excludeArea(ruleText.getArea())));
        });
        ref.set(sortableListWidget);
        return new ParentWidget<>()
                .size(1f, 1f)
                .child(sortableListWidget
                        .onRemove(stringItem -> {
                            this.availableElementsNbt.get(stringItem.getValue()).available = true;
                        })
                        .onChange(list -> {
                            BogoSorterConfig.nbtSortRules.clear();
                            BogoSorterConfig.nbtSortRules.addAll(list);
                        })
                        .left(7).right(7).top(7).bottom(23))
                .child(new ButtonWidget<>()
                        .bottom(7).size(12, 12).left(0.5f)
                        .background(GuiTextures.BUTTON, GuiTextures.ADD)
                        .onMousePressed(mouseButton -> {
                            if (!isPanelOpen("choose_nbt_rules")) {
                                ModularPanel panel1 = ModularPanel.defaultPanel(context, 200, 140).name("choose_nbt_rules");
                                openPanel(panel1
                                        .child(new ButtonWidget<>()
                                                .size(8, 8)
                                                .top(4).right(4)
                                                .background(GuiTextures.BUTTON, GuiTextures.CLOSE)
                                                .onMousePressed(mouseButton1 -> {
                                                    closePanel(panel1);
                                                    return true;
                                                }))
                                        .child(new Grid()
                                                .matrix(availableMatrix)
                                                .scrollable()
                                                .pos(7, 7).right(17).bottom(7)));
                            }
                            return true;
                        }));
    }

    @Override
    public void onClose() {
        super.onClose();
        Serializer.saveConfig();
        PlayerConfig.syncToServer();
        MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
        wasOpened = false;
    }

    private static class SortListItem<T extends SortRule<?>> extends SortableListWidget.Item<T> {

        private final IWidget ascendingToggle;

        public SortListItem(T value, IWidget content) {
            super(value, content);
            this.ascendingToggle = new CycleButtonWidget()
                    .toggle(getValue()::isInverted, getValue()::setInverted)
                    .background(GuiTextures.BUTTON)
                    .texture(ARROW_DOWN_UP)
                    .addTooltip(0, IKey.lang("bogosort.gui.descending"))
                    .addTooltip(1, IKey.lang("bogosort.gui.ascending"))
                    .height(1f).width(14).pos(0, 0);
            content.flex().left(14).right(10);
            removeable(buttonWidget -> buttonWidget.background(GuiTextures.BUTTON, GuiTextures.CLOSE.asIcon().size(8, 8)));
            getChildren().add(this.ascendingToggle);
        }
    }

    private static class AvailableElement extends ButtonWidget<AvailableElement> {

        private boolean available = true;
        private IDrawable[] activeBackground = {GuiTextures.BUTTON}, background = {GuiTextures.SLOT_DARK};

        @Override
        public AvailableElement background(IDrawable... background) {
            activeBackground = ArrayUtils.addAll(activeBackground, background);
            this.background = ArrayUtils.addAll(this.background, background);
            return this;
        }

        @Override
        public IDrawable[] getBackground() {
            return this.available ? activeBackground : background;
        }
    }
}
