package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.CrossAxisAlignment;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.TabButton;
import com.cleanroommc.modularui.widgets.TabContainer;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraftforge.common.MinecraftForge;

public class ConfigGui extends ModularScreen {

    public static boolean wasOpened = false;
    public static final UITexture TOGGLE_BUTTON = UITexture.fullImage("bogosorter:gui/toggle_config");
    public static final UITexture ARROW_DOWN_UP = UITexture.fullImage("bogosorter:gui/arrow_down_up");

    public ConfigGui() {
        super(BogoSorter.ID, "config");
    }

    @Override
    public ModularPanel buildUI(GuiContext guiContext) {
        ModularPanel panel = ModularPanel.defaultPanel(guiContext, 300, 250);

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
                .child(new Rectangle().setColor(Color.BLACK.bright(7)).asWidget()
                        .top(16)
                        .bottom(4)
                        .width(1)
                        .left(89))
                .child(new TabContainer()
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
                );

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

    public IWidget createItemSortConfigUI(GuiContext context) {
        return new ParentWidget<>().size(1f, 1f);
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
}
