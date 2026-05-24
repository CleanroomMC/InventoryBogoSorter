package com.cleanroommc.bogosorter.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import cpw.mods.fml.common.Loader;

public final class ThaumicEnergisticsHelper {

    private static final String MOD_ID = "thaumicenergistics";
    private static final String ESSENTIA_STACK_TYPE_CLASS = "thaumicenergistics.common.storage.AEEssentiaStackType";
    private static final String ESSENTIA_STACK_TYPE_FIELD = "ESSENTIA_STACK_TYPE";
    private static final String THAUMIC_ENERGISTICS_MAIN_CLASS = "thaumicenergistics.common.ThaumicEnergistics";

    private static Object essentiaStackType;

    private ThaumicEnergisticsHelper() {}

    public static boolean isLoaded() {
        return Loader.isModLoaded(MOD_ID);
    }

    public static String getAspectTag(ItemStack stack) {
        if (!isLoaded() || stack == null || stack.getItem() == null) {
            return null;
        }

        String tag = getAspectTagFromNbt(stack);
        if (tag != null) {
            return tag;
        }

        tag = getAspectTagFromStaticAspectMethod(
            "thaumicenergistics.common.items.ItemCraftingAspect",
            "getAspect",
            stack);
        if (tag != null) {
            return tag;
        }

        tag = getAspectTagFromStaticAspectMethod(
            "com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect",
            "getAspect",
            stack);
        if (tag != null) {
            return tag;
        }

        tag = getAspectTagFromAspectListMethod(
            "com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect",
            "getAspects",
            stack);
        if (tag != null) {
            return tag;
        }

        return getAspectTagFromEssentiaContainer(stack);
    }

    public static long getEssentiaAmount(IGrid grid, String aspectTag) {
        if (!isLoaded() || grid == null || aspectTag == null || aspectTag.isEmpty()) {
            return 0L;
        }

        try {
            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            if (storageGrid == null) {
                return 0L;
            }

            try {
                return getEssentiaAmountFromMonitor(storageGrid, aspectTag);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                return getEssentiaAmountFromCellProviders(storageGrid, aspectTag);
            }
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    private static String getAspectTagFromNbt(ItemStack stack) {
        return getAspectTagFromNbt(stack == null ? null : stack.getTagCompound());
    }

    private static String getAspectTagFromNbt(NBTTagCompound tag) {
        if (tag == null) {
            return null;
        }
        if (tag.hasKey("Aspect")) {
            String aspect = tag.getString("Aspect");
            return aspect.isEmpty() ? null : aspect;
        }
        if (tag.hasKey("AspectTag")) {
            String aspect = tag.getString("AspectTag");
            return aspect.isEmpty() ? null : aspect;
        }
        return null;
    }

    private static String getAspectTagFromStaticAspectMethod(String className, String methodName, ItemStack stack) {
        try {
            Class<?> itemClass = Class.forName(className);
            if (!itemClass.isInstance(stack.getItem())) {
                return null;
            }

            Method method = itemClass.getMethod(methodName, ItemStack.class);
            return tagOfAspect(method.invoke(null, stack));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String getAspectTagFromAspectListMethod(String className, String methodName, ItemStack stack) {
        try {
            Class<?> itemClass = Class.forName(className);
            if (!itemClass.isInstance(stack.getItem())) {
                return null;
            }

            Method method = itemClass.getMethod(methodName, ItemStack.class);
            return firstAspectTag(method.invoke(null, stack));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String getAspectTagFromEssentiaContainer(ItemStack stack) {
        try {
            Method method = stack.getItem()
                .getClass()
                .getMethod("getAspects", ItemStack.class);
            return firstAspectTag(method.invoke(stack.getItem(), stack));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String firstAspectTag(Object aspectList) throws ReflectiveOperationException {
        if (aspectList == null) {
            return null;
        }

        Method getAspects = findMethod(aspectList.getClass(), "getAspects");
        Object aspects = getAspects.invoke(aspectList);
        if (!(aspects instanceof Object[]) || ((Object[]) aspects).length == 0) {
            return null;
        }
        return tagOfAspect(((Object[]) aspects)[0]);
    }

    private static String tagOfAspect(Object aspect) throws ReflectiveOperationException {
        if (aspect == null) {
            return null;
        }

        Method getTag = aspect.getClass()
            .getMethod("getTag");
        Object tag = getTag.invoke(aspect);
        return tag instanceof String && !((String) tag).isEmpty() ? (String) tag : null;
    }

    private static Object getEssentiaStackType() throws ReflectiveOperationException {
        if (essentiaStackType != null) {
            return essentiaStackType;
        }

        Object directType = getStaticFieldValue(ESSENTIA_STACK_TYPE_CLASS, ESSENTIA_STACK_TYPE_FIELD);
        if (isEssentiaStackType(directType)) {
            essentiaStackType = directType;
            return essentiaStackType;
        }

        Object searchedType = findEssentiaStackTypeInStaticFields(
            ESSENTIA_STACK_TYPE_CLASS,
            THAUMIC_ENERGISTICS_MAIN_CLASS);
        if (searchedType != null) {
            essentiaStackType = searchedType;
            return essentiaStackType;
        }

        throw new ClassNotFoundException("registered AE essentia stack type");
    }

    private static long getEssentiaAmountFromMonitor(IStorageGrid storageGrid, String aspectTag)
        throws ReflectiveOperationException {
        Object stackType = getEssentiaStackType();
        Object requested = createEssentiaStack(aspectTag);
        if (stackType == null || requested == null) {
            return 0L;
        }

        Object monitor = invokeGetMEMonitor(storageGrid, stackType);
        Object storageList = monitor == null ? null
            : findAnyMethod(monitor.getClass(), "getStorageList").invoke(monitor);
        if (storageList == null) {
            return 0L;
        }

        Object found = invokeFindPrecise(storageList, requested);
        if (found == null) {
            return 0L;
        }

        Method getStackSize = findMethod(found.getClass(), "getStackSize");
        Object amount = getStackSize.invoke(found);
        return amount instanceof Number ? ((Number) amount).longValue() : 0L;
    }

    private static long getEssentiaAmountFromCellProviders(IStorageGrid storageGrid, String aspectTag)
        throws ReflectiveOperationException {
        Field field = findField(storageGrid.getClass(), "activeCellProviders");
        Object providers = field.get(storageGrid);
        if (!(providers instanceof Iterable<?>)) {
            return 0L;
        }

        long total = 0L;
        for (Object provider : (Iterable<?>) providers) {
            if (provider == null) {
                continue;
            }

            total += getEssentiaAmountFromProvider(provider, aspectTag);
            if (total < 0L) {
                return Long.MAX_VALUE;
            }
        }
        return total;
    }

    private static long getEssentiaAmountFromProvider(Object provider, String aspectTag)
        throws ReflectiveOperationException {
        if (hasMethod(provider.getClass(), "getCellCount") && hasMethod(provider.getClass(), "getInternalInventory")) {
            try {
                return getEssentiaAmountFromDriveLikeProvider(provider, aspectTag);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException(
                    e.getMessage() + " provider="
                        + provider.getClass()
                            .getName());
            }
        }

        if (hasMethod(provider.getClass(), "getStackInSlot", int.class)) {
            Object stack = invokeMethod(provider, "getStackInSlot", 1);
            return stack instanceof ItemStack ? getEssentiaAmountFromCellStack((ItemStack) stack, aspectTag) : 0L;
        }

        return 0L;
    }

    private static long getEssentiaAmountFromDriveLikeProvider(Object provider, String aspectTag)
        throws ReflectiveOperationException {
        Object inventory = invokeMethod(provider, "getInternalInventory");
        if (inventory == null) {
            return 0L;
        }

        if (inventory instanceof Iterable<?>) {
            return getEssentiaAmountFromInventoryIterable((Iterable<?>) inventory, aspectTag);
        }

        Object cellCount = invokeMethod(provider, "getCellCount");
        int slots = cellCount instanceof Number ? ((Number) cellCount).intValue() : 0;
        long total = 0L;
        for (int slot = 0; slot < slots; slot++) {
            Object stack;
            try {
                stack = invokeMethod(inventory, "getStackInSlot", slot);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException(
                    "getStackInSlot inventory=" + inventory.getClass()
                        .getName() + " message=" + e.getMessage());
            }
            if (stack instanceof ItemStack) {
                total += getEssentiaAmountFromCellStack((ItemStack) stack, aspectTag);
                if (total < 0L) {
                    return Long.MAX_VALUE;
                }
            }
        }
        return total;
    }

    private static long getEssentiaAmountFromInventoryIterable(Iterable<?> inventory, String aspectTag) {
        long total = 0L;
        for (Object stack : inventory) {
            if (stack instanceof ItemStack) {
                total += getEssentiaAmountFromCellStack((ItemStack) stack, aspectTag);
                if (total < 0L) {
                    return Long.MAX_VALUE;
                }
            }
        }
        return total;
    }

    private static long getEssentiaAmountFromCellStack(ItemStack stack, String aspectTag) {
        if (stack == null || stack.getItem() == null) {
            return 0L;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !containsEssentiaCellData(tag)) {
            return 0L;
        }

        long total = 0L;
        int storedTypes = tag.hasKey("et") ? tag.getShort("et") : countSequentialEssentiaEntries(tag);
        for (int i = 0; i < storedTypes; i++) {
            String key = "Essentia#" + i;
            if (!tag.hasKey(key)) {
                continue;
            }

            NBTTagCompound stackTag = tag.getCompoundTag(key);
            String stackAspectTag = getAspectTagFromNbt(stackTag);
            if (!aspectTag.equals(stackAspectTag)) {
                continue;
            }

            if (stackTag.hasKey("Cnt")) {
                total += stackTag.getLong("Cnt");
            } else if (stackTag.hasKey("Amount")) {
                total += stackTag.getLong("Amount");
            }

            if (total < 0L) {
                return Long.MAX_VALUE;
            }
        }
        return total;
    }

    private static Object createEssentiaStack(String aspectTag) throws ReflectiveOperationException {
        Object stackType = getEssentiaStackType();
        if (stackType == null) {
            return null;
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("AspectTag", aspectTag);
        tag.setLong("Cnt", 1L);

        Method loadStackFromNbt = findMethod(stackType.getClass(), "loadStackFromNBT", NBTTagCompound.class);
        return loadStackFromNbt.invoke(stackType, tag);
    }

    private static Object findEssentiaStackType(Object candidate) throws ReflectiveOperationException {
        if (candidate == null) {
            return null;
        }

        if (isEssentiaStackType(candidate)) {
            return candidate;
        }

        if (candidate instanceof Map<?, ?>) {
            for (Object value : ((Map<?, ?>) candidate).values()) {
                Object found = findEssentiaStackType(value);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        if (candidate instanceof Iterable<?>) {
            for (Object value : (Iterable<?>) candidate) {
                Object found = findEssentiaStackType(value);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        if (candidate.getClass()
            .isArray()) {
            int length = java.lang.reflect.Array.getLength(candidate);
            for (int i = 0; i < length; i++) {
                Object found = findEssentiaStackType(java.lang.reflect.Array.get(candidate, i));
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private static boolean isEssentiaStackType(Object candidate) {
        try {
            if (candidate == null) {
                return false;
            }
            Method getId = findMethod(candidate.getClass(), "getId");
            Object id = getId.invoke(candidate);
            return "essentia".equals(id);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Object findEssentiaStackTypeInStaticFields(String... classNames)
        throws ReflectiveOperationException {
        for (String className : classNames) {
            try {
                Class<?> type = Class.forName(className);
                for (Field field : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    field.setAccessible(true);
                    Object found = findEssentiaStackType(field.get(null));
                    if (found != null) {
                        return found;
                    }
                }
            } catch (ClassNotFoundException ignored) {
                // Ignore missing optional integration classes and continue checking other candidates.
            }
        }
        return null;
    }

    private static Object getStaticFieldValue(String className, String fieldName) {
        try {
            Class<?> type = Class.forName(className);
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object invokeFindPrecise(Object itemList, Object requested) throws ReflectiveOperationException {
        Method findPrecise;
        try {
            findPrecise = itemList.getClass()
                .getMethod("findPrecise", requested.getClass());
        } catch (NoSuchMethodException ignored) {
            findPrecise = findMethod(itemList.getClass(), "findPrecise", Object.class);
        }
        return findPrecise.invoke(itemList, requested);
    }

    private static Object invokeGetMEMonitor(IStorageGrid storageGrid, Object stackType)
        throws ReflectiveOperationException {
        for (Method method : allMethods(storageGrid.getClass())) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (!"getMEMonitor".equals(method.getName()) || parameterTypes.length != 1) {
                continue;
            }

            try {
                method.setAccessible(true);
                return method.invoke(storageGrid, stackType);
            } catch (IllegalArgumentException ignored) {
                // Try the next overload/interface method. Some AE2 builds expose this only through extended APIs.
            }
        }
        throw new NoSuchMethodException("getMEMonitor");
    }

    private static Method findAnyMethod(Class<?> type, String name) throws NoSuchMethodException {
        for (Method method : allMethods(type)) {
            if (name.equals(method.getName()) && method.getParameterTypes().length == 0) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static Method[] allMethods(Class<?> type) {
        java.util.List<Method> methods = new java.util.ArrayList<>();
        Class<?> current = type;
        while (current != null) {
            java.util.Collections.addAll(methods, current.getDeclaredMethods());
            for (Class<?> interfaceType : current.getInterfaces()) {
                java.util.Collections.addAll(methods, interfaceType.getMethods());
            }
            current = current.getSuperclass();
        }
        java.util.Collections.addAll(methods, type.getMethods());
        return methods.toArray(new Method[methods.size()]);
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }

        for (Method method : type.getMethods()) {
            if (!name.equals(method.getName())) {
                continue;
            }
            if (!java.util.Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                continue;
            }
            method.setAccessible(true);
            return method;
        }

        throw new NoSuchMethodException(name);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static boolean hasMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            findMethod(type, name, parameterTypes);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static Object invokeMethod(Object target, String name, Object... args) throws ReflectiveOperationException {
        Method method = findCompatibleMethod(target.getClass(), name, args);
        if (method == null) {
            throw new NoSuchMethodException(
                name + " on "
                    + target.getClass()
                        .getName());
        }
        return method.invoke(target, args);
    }

    private static Method findCompatibleMethod(Class<?> type, String name, Object... args) {
        for (Method method : allMethods(type)) {
            if (!name.equals(method.getName())) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != args.length) {
                continue;
            }

            boolean compatible = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!isArgumentCompatible(parameterTypes[i], args[i])) {
                    compatible = false;
                    break;
                }
            }

            if (!compatible) {
                continue;
            }

            method.setAccessible(true);
            return method;
        }
        return null;
    }

    private static boolean isArgumentCompatible(Class<?> parameterType, Object value) {
        if (value == null) {
            return !parameterType.isPrimitive();
        }

        if (parameterType.isPrimitive()) {
            parameterType = wrapPrimitiveType(parameterType);
        }

        return parameterType.isInstance(value);
    }

    private static Class<?> wrapPrimitiveType(Class<?> type) {
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }

    private static boolean containsEssentiaCellData(NBTTagCompound tag) {
        return tag.hasKey("Essentia#0") || tag.hasKey("et") || tag.hasKey("ec");
    }

    private static int countSequentialEssentiaEntries(NBTTagCompound tag) {
        int count = 0;
        while (tag.hasKey("Essentia#" + count)) {
            count++;
        }
        return count;
    }
}
