package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.modularui.widgets.layout.Row;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigGuiOld {

    public static boolean wasOpened = false;
    /*public static final UITexture TOGGLE_BUTTON = UITexture.fullImage("bogosorter:gui/toggle_config");
    public static final UITexture ARROW_DOWN_UP = UITexture.fullImage("bogosorter:gui/arrow_down_up");

    public static ModularWindow createConfigWindow(UIBuildContext buildContext) {
        buildContext.setShowJei(false);
        buildContext.addCloseListener(() -> {
            Serializer.saveConfig();
            PlayerConfig.syncToServer();
            MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
            wasOpened = false;
        });
        ModularWindow.Builder builder = ModularWindow.builder(300, 250);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND)
                .widget(new TextWidget(Text.localised("bogosort.gui.title"))
                        .setTextAlignment(Alignment.Center)
                        .setPos(0, 5)
                        .setSize(300, 11))
                .widget(new Rectangle().setColor(Color.BLACK.bright(7)).asWidget()
                        .setPos(89, 16)
                        .setSize(1, 231))
                .widget(new Rectangle().setColor(Color.BLACK.bright(7)).asWidget()
                        .setPos(3, 16)
                        .setSize(294, 1))
                .widget(new TabContainer()
                        .addTabButton(new TabButton(0)
                                .setBackground(false, new Rectangle().setColor(0x00000001), Text.localised("bogosort.gui.tab.general.name").color(Color.WHITE.normal).shadow())
                                .setBackground(true, new Rectangle().setColor(0xFFb1b1b1), Text.localised("bogosort.gui.tab.general.name").color(Color.WHITE.normal).shadow())
                                .setSize(86, 16)
                                .setPos(-87, 0))
                        .addTabButton(new TabButton(1)
                                .setBackground(false, new Rectangle().setColor(0x00000001), Text.localised("bogosort.gui.tab.item_sort_rules.name").color(Color.WHITE.normal).shadow())
                                .setBackground(true, new Rectangle().setColor(0xFFb1b1b1), Text.localised("bogosort.gui.tab.item_sort_rules.name").color(Color.WHITE.normal).shadow())
                                .setSize(86, 16)
                                .setPos(-87, 16))
                        .addTabButton(new TabButton(2)
                                .setBackground(false, new Rectangle().setColor(0x00000001), Text.localised("bogosort.gui.tab.nbt_sort_rules.name").color(Color.WHITE.normal).shadow())
                                .setBackground(true, new Rectangle().setColor(0xFFb1b1b1), Text.localised("bogosort.gui.tab.nbt_sort_rules.name").color(Color.WHITE.normal).shadow())
                                .setSize(86, 16)
                                .setPos(-87, 32))
                        .addPage(createGeneralConfigUI(buildContext))
                        .addPage(createItemSortConfigUI(buildContext))
                        .addPage(createNbtSortConfigUI(buildContext))
                        .setSize(210, 236)
                        .setPos(90, 17));

        wasOpened = true;
        return builder.build();
    }

    private static Widget createGeneralConfigUI(UIBuildContext buildContext) {
        TextFieldWidget dmgThresholdField = new TextFieldWidget();
        dmgThresholdField.setText(String.valueOf(PlayerConfig.getClient().autoRefillDamageThreshold));
        return new Scrollable()
                .setVerticalScroll()
                .widget(new Rectangle().setColor(0xFF606060).asWidget()
                        .setSize(1, 30)
                        .setPos(32, 0))
                .widget(new MultiChildWidget()
                        .addChild(new CycleButtonWidget()
                                .setToggle(() -> PlayerConfig.getClient().enableAutoRefill, val -> PlayerConfig.getClient().enableAutoRefill = val)
                                .setTexture(TOGGLE_BUTTON)
                                .setSize(14, 14)
                                .setPos(8, 0))
                        .addChild(new TextWidget(Text.localised("bogosort.gui.enable_refill"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(160, 14)
                                .setPos(35, 0))
                        .setPos(0, 0))
                .widget(new MultiChildWidget()
                        .addChild(dmgThresholdField
                                .setGetterInt(() -> PlayerConfig.getClient().autoRefillDamageThreshold)
                                .setSetterInt(val -> PlayerConfig.getClient().autoRefillDamageThreshold = val)
                                .setNumbers(1, Short.MAX_VALUE)
                                .setTextAlignment(Alignment.Center)
                                .setSize(30, 14))
                        .addChild(new TextWidget(Text.localised("bogosort.gui.refill_threshold"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(160, 14)
                                .setPos(35, 0))
                        .setPos(0, 18))
                .widget(new MultiChildWidget()
                        .addChild(new TextWidget(Text.localised("bogosort.gui.hotbar_scrolling"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .addTooltip(Text.localised("bogosort.gui.hotbar_scrolling.tooltip"))
                                .setTooltipShowUpDelay(10)
                                .setSize(160, 14)
                                .setPos(5, 0))
                        .addChild(new CycleButtonWidget()
                                .setToggle(HotbarSwap::isEnabled, HotbarSwap::setEnabled)
                                .setTexture(TOGGLE_BUTTON)
                                .setSize(14, 14)
                                .setPos(8, 16))
                        .addChild(new TextWidget(Text.localised("bogosort.gui.enabled"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(160, 14)
                                .setPos(35, 16))
                        .setPos(0, 40))
                .setSize(200, 220)
                .setPos(5, 5);
    }

    private static Widget createItemSortConfigUI(UIBuildContext buildContext) {
        SortableListWidget<SortRule<ItemStack>> sortableListWidget = SortableListWidget.removable(BogoSortAPI.INSTANCE.getItemSortRuleList(), BogoSorterConfig.sortRules);
        Map<SortRule<ItemStack>, AvailableListItem<SortRule<ItemStack>>> widgetMap = new HashMap<>();
        for (SortRule<ItemStack> sortRule : BogoSortAPI.INSTANCE.getItemSortRuleList()) {
            AvailableListItem<SortRule<ItemStack>> listItem = new AvailableListItem<>(sortRule, new TextWidget(Text.localised(sortRule.getNameLangKey()).color(Color.WHITE.normal).shadow())
                    .addTooltip(Text.localised(sortRule.getDescriptionLangKey()))
                    .setTooltipShowUpDelay(10)
                    .setSize(80, 20));
            listItem.setAvailable(!BogoSorterConfig.sortRules.contains(sortRule))
                    .setMoveConsumer(clickData -> sortableListWidget.addElement(sortRule))
                    .setAvailableBackground(ModularUITextures.BASE_BUTTON)
                    .setUnavailableBackground(ModularUITextures.FLUID_SLOT)
                    .setSize(90, 20);
            widgetMap.put(sortRule, listItem);
        }

        List<Widget> orderedWidgetList = BogoSortAPI.INSTANCE.getItemSortRuleList().stream().map(widgetMap::get).collect(Collectors.toList());

        return new MultiChildWidget()
                .addChild(new TextWidget(Text.localised("bogosort.gui.available_sort_rules"))
                        .setTextAlignment(Alignment.Center)
                        .setPos(5, 5)
                        .setSize(90, 18))
                .addChild(new TextWidget(Text.localised("bogosort.gui.configured_sort_rules"))
                        .setTextAlignment(Alignment.Center)
                        .setPos(105, 5)
                        .setSize(100, 18))
                .addChild(ListWidget.builder(new ArrayList<>(orderedWidgetList), (widget, index) -> widget)
                        .setPos(5, 24)
                        .setSize(90, 200))
                .addChild(sortableListWidget
                        .setWidgetCreator(ConfigGuiOld::makeSortRuleWidget)
                        .setSaveFunction(list -> {
                            BogoSorterConfig.sortRules.clear();
                            BogoSorterConfig.sortRules.addAll(list);
                        })
                        .setOnRemoveElement(sortRule -> widgetMap.get(sortRule).setAvailable(true))
                        .setPos(105, 24)
                        .setSize(100, 200));
    }

    private static Widget createNbtSortConfigUI(UIBuildContext buildContext) {
        SortableListWidget<NbtSortRule> sortableListWidget = SortableListWidget.removable(BogoSortAPI.INSTANCE.getNbtSortRuleList(), BogoSorterConfig.nbtSortRules);
        Map<NbtSortRule, AvailableListItem<NbtSortRule>> widgetMap = new HashMap<>();
        for (NbtSortRule sortRule : BogoSortAPI.INSTANCE.getNbtSortRuleList()) {
            AvailableListItem<NbtSortRule> listItem = new AvailableListItem<>(sortRule, new TextWidget(Text.localised(sortRule.getNameLangKey()).color(Color.WHITE.normal).shadow())
                    .addTooltip(Text.localised(sortRule.getDescriptionLangKey()))
                    .setTooltipShowUpDelay(10)
                    .setSize(80, 20));
            listItem.setAvailable(!BogoSorterConfig.nbtSortRules.contains(sortRule))
                    .setMoveConsumer(clickData -> sortableListWidget.addElement(sortRule))
                    .setAvailableBackground(ModularUITextures.BASE_BUTTON)
                    .setUnavailableBackground(ModularUITextures.FLUID_SLOT)
                    .setSize(90, 20);
            widgetMap.put(sortRule, listItem);
        }

        List<Widget> orderedWidgetList = BogoSortAPI.INSTANCE.getNbtSortRuleList().stream().map(widgetMap::get).collect(Collectors.toList());

        return new MultiChildWidget()
                .addChild(new TextWidget(Text.localised("bogosort.gui.available_sort_rules"))
                        .setTextAlignment(Alignment.Center)
                        .setPos(5, 5)
                        .setSize(90, 18))
                .addChild(new TextWidget(Text.localised("bogosort.gui.configured_sort_rules"))
                        .setTextAlignment(Alignment.Center)
                        .setPos(105, 5)
                        .setSize(100, 18))
                .addChild(ListWidget.builder(new ArrayList<>(orderedWidgetList), (widget, index) -> widget)
                        .setPos(5, 24)
                        .setSize(90, 200))
                .addChild(sortableListWidget
                        .setWidgetCreator(ConfigGuiOld::makeSortRuleWidget)
                        .setSaveFunction(list -> {
                            BogoSorterConfig.nbtSortRules.clear();
                            BogoSorterConfig.nbtSortRules.addAll(list);
                        })
                        .setOnRemoveElement(sortRule -> widgetMap.get(sortRule).setAvailable(true))
                        .setPos(105, 24)
                        .setSize(100, 200));
    }

    private static Widget makeSortRuleWidget(SortRule<?> sortRule) {
        return new Row()
                .addChild(new CycleButtonWidget()
                        .setToggle(sortRule::isInverted, sortRule::setInverted)
                        .setTexture(ARROW_DOWN_UP)
                        .addTooltip(0, Text.localised("bogosort.gui.descending"))
                        .addTooltip(1, Text.localised("bogosort.gui.ascending"))
                        .setBackground(ModularUITextures.BASE_BUTTON)
                        .setSize(14, 20))
                .addChild(new TextWidget(Text.localised(sortRule.getNameLangKey()).color(Color.WHITE.normal).shadow())
                        .setTextAlignment(Alignment.Center)
                        .addTooltip(Text.localised(sortRule.getDescriptionLangKey()))
                        .setTooltipShowUpDelay(10)
                        .setBackground(ModularUITextures.BASE_BUTTON)
                        .setSize(66, 20))
                .setSize(80, 20);
    }

    private static Widget createOrePrefixConfigUI(UIBuildContext buildContext) {
        return new MultiChildWidget();
    }*/
}
