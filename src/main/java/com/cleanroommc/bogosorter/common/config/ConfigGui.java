package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.UITexture;
import com.cleanroommc.modularui.api.drawable.shapes.Rectangle;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.*;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigGui {

    public static boolean wasOpened = false;
    public static final UITexture TOGGLE_BUTTON = UITexture.fullImage("bogosorter:gui/toggle_config");

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
        dmgThresholdField.setText(String.valueOf(PlayerConfig.CLIENT.autoRefillDamageThreshold));
        return new Scrollable()
                .setVerticalScroll()
                .widget(new Rectangle().setColor(0xFF606060).asWidget()
                        .setSize(1, 200)
                        .setPos(32, 0))
                .widget(new MultiChildWidget()
                        .addChild(new CycleButtonWidget()
                                .setToggle(() -> PlayerConfig.CLIENT.enableAutoRefill, val -> PlayerConfig.CLIENT.enableAutoRefill = val)
                                .setTexture(TOGGLE_BUTTON)
                                .setSize(14, 14)
                                .setPos(8, 0))
                        .addChild(new TextWidget(Text.localised("Enable auto hotbar refill"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(160, 14)
                                .setPos(35, 0))
                        .setPos(0, 0))
                .widget(new MultiChildWidget()
                        .addChild(dmgThresholdField
                                .setGetterInt(() -> PlayerConfig.CLIENT.autoRefillDamageThreshold)
                                .setSetterInt(val -> PlayerConfig.CLIENT.autoRefillDamageThreshold = val)
                                .setNumbers(1, Short.MAX_VALUE)
                                .setTextAlignment(Alignment.Center)
                                .setSize(30, 14))
                        .addChild(new TextWidget(Text.localised("Auto refill damage threshold"))
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(160, 14)
                                .setPos(35, 0))
                        .setPos(0, 18))
                .setSize(200, 220)
                .setPos(5, 5);
    }

    private static Widget createItemSortConfigUI(UIBuildContext buildContext) {
        SortableListWidget<SortRule<ItemStack>> sortableListWidget = SortableListWidget.removable(BogoSortAPI.INSTANCE.getItemSortRuleList(), BogoSorterConfig.sortRules);
        Map<SortRule<ItemStack>, AvailableListItem<SortRule<ItemStack>>> widgetMap = new HashMap<>();
        for (SortRule<ItemStack> sortRule : BogoSortAPI.INSTANCE.getItemSortRuleList()) {
            AvailableListItem<SortRule<ItemStack>> listItem = new AvailableListItem<>(sortRule, new TextWidget(Text.localised(sortRule.getNameLangKey()).color(Color.WHITE.normal).shadow())
                    .addTooltip(Text.localised(sortRule.getDescriptionLangKey()))
                    .setTooltipShowUpDelay(20)
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
                        .setWidgetCreator(sortRule -> new TextWidget(Text.localised(sortRule.getNameLangKey()).color(Color.WHITE.normal).shadow())
                                .setTextAlignment(Alignment.Center)
                                .addTooltip(Text.localised(sortRule.getDescriptionLangKey()))
                                .setTooltipShowUpDelay(20)
                                .setBackground(ModularUITextures.BASE_BUTTON)
                                .setSize(80, 20))
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
                    .setTooltipShowUpDelay(20)
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
                        .setWidgetCreator(sortRule -> new TextWidget(Text.localised(sortRule.getNameLangKey()).color(Color.WHITE.normal).shadow())
                                .setTextAlignment(Alignment.Center)
                                .addTooltip(Text.localised(sortRule.getDescriptionLangKey()))
                                .setTooltipShowUpDelay(20)
                                .setBackground(ModularUITextures.BASE_BUTTON)
                                .setSize(80, 20))
                        .setSaveFunction(list -> {
                            BogoSorterConfig.nbtSortRules.clear();
                            BogoSorterConfig.nbtSortRules.addAll(list);
                        })
                        .setOnRemoveElement(sortRule -> widgetMap.get(sortRule).setAvailable(true))
                        .setPos(105, 24)
                        .setSize(100, 200));
    }

    private static Widget createOrePrefixConfigUI(UIBuildContext buildContext) {
        return new MultiChildWidget();
    }
}
