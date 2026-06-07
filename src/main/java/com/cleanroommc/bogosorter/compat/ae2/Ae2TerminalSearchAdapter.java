package com.cleanroommc.bogosorter.compat.ae2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.common.config.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.compat.Mods;

import cpw.mods.fml.common.Loader;

public final class Ae2TerminalSearchAdapter {

    private static final Class<?>[] NO_PARAMETERS = new Class<?>[0];
    private static final Class<?>[] STRING_PARAMETER = new Class<?>[] { String.class };
    private static final String AE2_MONITORABLE_GUI_CLASS = "appeng.client.gui.implementations.GuiMEMonitorable";
    private static final String AE2_NEI_MODULES_CLASS = "appeng.integration.modules.NEI";
    private static final String NEI_CONTAINER_MANAGER_CLASS = "codechicken.nei.guihook.GuiContainerManager";
    private static final String NEI_SEARCH_FIELD_CLASS = "codechicken.nei.SearchField";
    private static final int MIN_SEARCH_RESULT_COUNT = 1;
    private static final Set<String> LOGGED_FAILURES = new HashSet<>();

    private Ae2TerminalSearchAdapter() {}

    public static boolean handleSearchKey(GuiContainer gui) {
        if (!TooltipFeatureConfig.isTerminalHoverSearchEnabled() || !Mods.Ae2.isLoaded()) {
            return false;
        }
        if (!Keyboard.getEventKeyState() || Keyboard.getEventKey() != BSKeybinds.ae2TerminalSearchKey.getKeyCode()) {
            return false;
        }

        GuiContainer ae2Gui = getAe2SearchTargetGui(gui);
        if (ae2Gui == null) {
            return false;
        }

        try {
            ItemStack hoveredStack = getHoveredStack(gui);
            if (hoveredStack == null) {
                return false;
            }

            Object searchField = getFieldValue(ae2Gui, "searchField");
            Object repo = getFieldValue(ae2Gui, "repo");
            if (searchField == null || repo == null || isAe2SearchActive(searchField)) {
                return false;
            }

            String displayName = hoveredStack.getDisplayName();
            if (displayName == null || displayName.trim()
                .isEmpty()) {
                return false;
            }

            String searchText = getEscapedNeiSearchText(displayName);
            String previousText = (String) invokeMethod(searchField, "getText");

            invokeMethod(repo, "setSearchString", STRING_PARAMETER, searchText);
            invokeMethod(repo, "updateView");
            Object size = invokeMethod(repo, "size");
            if (!(size instanceof Integer) || (Integer) size < MIN_SEARCH_RESULT_COUNT) {
                invokeMethod(repo, "setSearchString", STRING_PARAMETER, previousText);
                invokeMethod(repo, "updateView");
                return false;
            }

            invokeMethod(searchField, "setText", STRING_PARAMETER, searchText);
            invokeMethod(searchField, "setCursorPositionEnd");
            return true;
        } catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
            logFailureOnce("terminal-search", e);
            return false;
        }
    }

    @Nullable
    private static GuiContainer getAe2SearchTargetGui(GuiContainer gui) {
        if (isAe2MonitorableGui(gui)) {
            return gui;
        }

        try {
            Object firstGui = getFieldValue(gui, "firstGui");
            if (firstGui instanceof GuiContainer && isAe2MonitorableGui((GuiContainer) firstGui)) {
                return (GuiContainer) firstGui;
            }
        } catch (ReflectiveOperationException e) {
            logFailureOnce("firstGui-field", e);
        }

        try {
            Object firstGui = invokeMethod(gui, "getFirstScreen");
            if (firstGui instanceof GuiContainer && isAe2MonitorableGui((GuiContainer) firstGui)) {
                return (GuiContainer) firstGui;
            }
        } catch (ReflectiveOperationException e) {
            logFailureOnce("getFirstScreen-method", e);
        }

        return null;
    }

    private static boolean isAe2MonitorableGui(GuiContainer gui) {
        Class<?> current = gui.getClass();
        while (current != null) {
            if (AE2_MONITORABLE_GUI_CLASS.equals(current.getName())) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private static boolean isAe2SearchActive(Object searchField) {
        try {
            if (Boolean.TRUE.equals(invokeMethod(searchField, "isFocused"))) {
                return true;
            }
            Class<?> nei = Class.forName(AE2_NEI_MODULES_CLASS);
            Object ae2NeiSearchField = getStaticFieldValue(nei, "searchField");
            return ae2NeiSearchField != null
                && Boolean.TRUE.equals(invokeMethod(ae2NeiSearchField, "existsSearchField"))
                && Boolean.TRUE.equals(invokeMethod(ae2NeiSearchField, "focused"));
        } catch (ReflectiveOperationException | LinkageError e) {
            logFailureOnce("ae2-nei-search-focus", e);
            return false;
        }
    }

    @Nullable
    private static ItemStack getHoveredStack(GuiContainer gui) {
        if (Loader.isModLoaded("NotEnoughItems")) {
            try {
                Class<?> manager = Class.forName(NEI_CONTAINER_MANAGER_CLASS);
                Method method = manager.getMethod("getStackMouseOver", GuiContainer.class);
                Object result = method.invoke(null, gui);
                if (result instanceof ItemStack) {
                    return (ItemStack) result;
                }
            } catch (ReflectiveOperationException | LinkageError e) {
                logFailureOnce("nei-hovered-stack", e);
            }
        }
        return gui.theSlot == null ? null : gui.theSlot.getStack();
    }

    private static String getEscapedNeiSearchText(String text) {
        try {
            Class<?> searchField = Class.forName(NEI_SEARCH_FIELD_CLASS);
            Method method = searchField.getMethod("getEscapedSearchText", String.class);
            Object result = method.invoke(null, text);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (ReflectiveOperationException | LinkageError e) {
            logFailureOnce("nei-search-escape", e);
        }
        return text;
    }

    @Nullable
    private static Object getFieldValue(Object instance, String fieldName) throws ReflectiveOperationException {
        Class<?> current = instance.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @Nullable
    private static Object getStaticFieldValue(Class<?> type, String fieldName) throws ReflectiveOperationException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(null);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @Nullable
    private static Object invokeMethod(Object instance, String methodName) throws ReflectiveOperationException {
        return invokeMethod(instance, methodName, NO_PARAMETERS);
    }

    @Nullable
    private static Object invokeMethod(Object instance, String methodName, Class<?>[] paramTypes, Object... args)
        throws ReflectiveOperationException {
        Class<?> current = instance.getClass();
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method.invoke(instance, args);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        Method method = instance.getClass()
            .getMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(instance, args);
    }

    private static void logFailureOnce(String capability, Throwable throwable) {
        if (TooltipFeatureConfig.isDebugLoggingEnabled() && LOGGED_FAILURES.add(capability)) {
            BogoSorter.LOGGER.warn("AE2 terminal hover search capability {} failed", capability, throwable);
        }
    }
}
