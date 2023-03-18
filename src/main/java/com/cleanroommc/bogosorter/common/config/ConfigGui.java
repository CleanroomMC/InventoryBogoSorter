package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.CrossAxisAlignment;
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
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Column;
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

    public ConfigGui() {
        super(BogoSorter.ID, "config");
    }

    @Override
    public ModularPanel buildUI(GuiContext guiContext) {
        this.availableElements = new Object2ObjectOpenHashMap<>();
        ModularPanel panel = ModularPanel.defaultPanel(guiContext, 300, 250);

        PagedWidget.Controller controller = new PagedWidget.Controller();

        Rectangle activeTab = new Rectangle().setColor(0xFFb1b1b1);
        Rectangle tab = new Rectangle().setColor(0x00000001);

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
                        .top(34).bottom(4)
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
                                .background(true, GuiTextures.BUTTON, IKey.lang("bogosort.gui.tab.item_sort_rules.name").color(Color.WHITE.normal).shadow(true))
                                .background(false, GuiTextures.SLOT_DARK, IKey.lang("bogosort.gui.tab.item_sort_rules.name").color(Color.WHITE.normal).shadow(true))))
                /*.child(new TabContainer()
                        .left(90)
                        .right(4)
                        .top(17)
                        .bottom(4)
                        .tabButtonSize(86, 16)
                        .buttonBarSide(TabContainer.Side.LEFT)
                        .tabButton(new TabButton(0)
                                .background(tab, IKey.lang("bogosort.gui.tab.general.name"))
                                .activeBackground(activeTab, IKey.lang("bogosort.gui.tab.general.name"))
                                .textureInset(-1))
                        .tabButton(new TabButton(1)
                                .background(tab, IKey.lang("bogosort.gui.tab.item_sort_rules.name"))
                                .activeBackground(activeTab, IKey.lang("bogosort.gui.tab.item_sort_rules.name"))
                                .textureInset(-1))
                        .tabButton(new TabButton(2)
                                .background(tab, IKey.lang("bogosort.gui.tab.nbt_sort_rules.name"))
                                .activeBackground(activeTab, IKey.lang("bogosort.gui.tab.nbt_sort_rules.name"))
                                .textureInset(-1))
                        .addPage(createGeneralConfigUI(guiContext))
                        .addPage(createItemSortConfigUI(guiContext))
                        .addPage(createNbtSortConfigUI(guiContext))
                )*/;

        return panel;
    }

    public IWidget createGeneralConfigUI(GuiContext context) {
        return new ScrollWidget<>()
                .size(1f, 1f)
                .child(new Column()
                        .crossAxisAlignment(CrossAxisAlignment.START)
                        .child(new Rectangle().setColor(0xFF606060).asWidget()
                                .top(1)
                                .left(32)
                                .size(1, 40))
                        .child(new Row()
                                .coverChildrenHeight()
                                .child(new CycleButtonWidget()
                                        .toggle(() -> PlayerConfig.getClient().enableAutoRefill, val -> PlayerConfig.getClient().enableAutoRefill = val)
                                        .texture(TOGGLE_BUTTON)
                                        .size(14, 14)
                                        .margin(8, 4))
                                .child(IKey.lang("bogosort.gui.enable_refill").asWidget()
                                        .height(14)
                                        .marginLeft(10)))
                        .child(new Row()
                                .margin(0, 4)
                                .coverChildrenHeight()
                                .child(new TextFieldWidget()
                                        .getterLong(() -> PlayerConfig.getClient().autoRefillDamageThreshold)
                                        .setterLong(val -> PlayerConfig.getClient().autoRefillDamageThreshold = (int) val)
                                        .setNumbers(1, Short.MAX_VALUE)
                                        .setTextAlignment(Alignment.Center)
                                        .background(new Rectangle().setColor(0xFFb1b1b1))
                                        .size(30, 14)
                                        .marginLeft(1))
                                .child(IKey.lang("bogosort.gui.refill_threshold").asWidget()
                                        .height(14)
                                        .alignment(Alignment.CenterLeft)
                                        .marginLeft(10)))
                        .child(new TextWidget(IKey.lang("bogosort.gui.hotbar_scrolling"))
                                .alignment(Alignment.CenterLeft)
                                .left(5).height(14)
                                .tooltip(tooltip -> tooltip.showUpTimer(10)
                                        .addLine(IKey.lang("bogosort.gui.hotbar_scrolling.tooltip"))))
                        .child(new Row()
                                .coverChildrenHeight()
                                .child(new CycleButtonWidget()
                                        .toggle(HotbarSwap::isEnabled, HotbarSwap::setEnabled)
                                        .texture(TOGGLE_BUTTON)
                                        .size(14, 14)
                                        .margin(8, 4))
                                .child(new TextWidget(IKey.lang("bogosort.gui.enabled"))
                                        .alignment(Alignment.CenterLeft)
                                        .height(14))));

    }

    public IWidget createProfilesConfig(GuiContext context) {
        PagedWidget.Controller controller = new PagedWidget.Controller();
        return new ParentWidget<>()
                .width(1f).top(1).bottom(0)
                .child(new Rectangle().setColor(Color.BLACK.bright(7)).asWidget()
                        .top(0)
                        .bottom(4)
                        .width(1)
                        .left(89))
                .child(new ListWidget<>() // Profiles
                        .pos(0, 0)
                        .width(89).bottom(0)
                        .child(new ButtonWidget<>()
                                .width(1f).height(16)
                                .background(GuiTextures.BUTTON, IKey.str("Profile 1"))))
                .child(new Row()
                        .left(90).right(0)
                        .height(16).top(0)
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
                            if (!isPanelOpen("Option Selection")) {
                                ModularPanel panel1 = ModularPanel.defaultPanel(context, 200, 140).name("Option Selection");
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
        return new ParentWidget<>().size(1f, 1f);
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
