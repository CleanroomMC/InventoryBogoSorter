package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.shapes.Rectangle;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.*;

public class ConfigGui {

    public static ModularWindow createConfigWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(300, 250);
        builder.setBackground(new Rectangle().setColor(Color.withAlpha(Color.BLACK.bright(7), 175)).setCornerRadius(10))
                .widget(new TextWidget("SortConfig")
                        .setDefaultColor(Color.WHITE.normal)
                        .setTextAlignment(Alignment.Center)
                        .setPos(0, 5)
                        .setSize(300, 11))
                .widget(new TabContainer()
                        .addTabButton(new TabButton(0)
                                .setBackground(new Rectangle().setColor(Color.BLACK.bright(5)), new Text("Item sort rules").color(Color.WHITE.normal))
                                .setSize(90, 14)
                                .setPos(-90, 14))
                        .addTabButton(new TabButton(1)
                                .setBackground(new Rectangle().setColor(Color.BLACK.bright(5)), new Text("NBT sort rules").color(Color.WHITE.normal))
                                .setSize(90, 14)
                                .setPos(-90, 28))
                        .addPage(createItemSortConfigUI(buildContext))
                        .addPage(createNbtSortConfigUI(buildContext))
                        .setSize(210, 236)
                        .setPos(90, 14));

        return builder.build();
    }

    private static Widget createItemSortConfigUI(UIBuildContext buildContext) {
        return new MultiChildWidget()
                .addChild(new SortableListWidget<>(SortHandler.getItemSortRules())
                        .setWidgetCreator(sortRule -> new TextWidget(sortRule.getKey())
                                .setTextAlignment(Alignment.Center)
                                .setBackground(ModularUITextures.BASE_BUTTON)
                                .setSize(80, 20))
                        .setSaveFunction(list -> {
                            SortHandler.getItemSortRules().clear();
                            SortHandler.getItemSortRules().addAll(list);
                            BogoSorter.SERIALIZER.saveConfig();
                        })
                        .setPos(5, 5)
                        .setSize(120, 200));
    }

    private static Widget createNbtSortConfigUI(UIBuildContext buildContext) {
        return new MultiChildWidget()
                .addChild(new SortableListWidget<>(SortHandler.getNbtSortRules())
                        .setWidgetCreator(sortRule -> new TextWidget(sortRule.getKey())
                                .setTextAlignment(Alignment.Center)
                                .setBackground(ModularUITextures.BASE_BUTTON)
                                .setSize(80, 20))
                        .setSaveFunction(list -> {
                            SortHandler.getNbtSortRules().clear();
                            SortHandler.getNbtSortRules().addAll(list);
                            BogoSorter.SERIALIZER.saveConfig();
                        })
                        .setPos(5, 5)
                        .setSize(120, 200));
    }

    private static Widget createOrePrefixConfigUI(UIBuildContext buildContext) {
        return new MultiChildWidget();
    }
}
