package com.cleanroommc.bogosorter.api;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.common.sort.ItemSortContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

@ApiStatus.NonExtendable
public interface IBogoSortAPI {

    static IBogoSortAPI getInstance() {
        return BogoSortAPI.INSTANCE;
    }

    /**
     * Adds sorting compat for a container class
     *
     * @param clazz   container class
     * @param builder sorting compat builder
     * @param <T>     container type
     */
    <T extends Container> void addCompat(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder);

    /**
     * Adds sorting compat for a container class.
     * Is useful when you don't have access to the super class of {@link T}
     *
     * @param clazz   container class
     * @param builder sorting compat builder
     * @param <T>     container type
     */
    @ApiStatus.Internal
    <T> void addCompatSimple(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder);

    /**
     * Removes sorting compat for a container class
     *
     * @param clazz container class
     * @param <T>   container type
     */
    <T extends Container> void removeCompat(Class<T> clazz);

    /**
     * Registers a sorting rule for items
     *
     * @param type           the type of which component will be compared
     * @param itemComparator comparator
     */
    void registerItemSortingRule(String key, SortType type, Comparator<ItemStack> itemComparator);

    /**
     * This registers a sort rule which can compare client data like localized name or color on server side.
     * This method is not API friendly! You will not get it to work by simply registering a rule. It is much more involved.
     * Instead, contact the author (brachy84) if you want to add a client sort rule.
     */
    @ApiStatus.Internal
    void registerClientItemSortingRule(String key, SortType type, Comparator<ItemStack> comparator, Comparator<ItemSortContainer> serverComparator);

    /**
     * Registers a sorting rule for NBT tags
     *
     * @param tagPath    path of the nbt tag. Separate sub tags with '/'
     * @param comparator comparator sorting the tags based on tagPath
     */
    void registerNbtSortingRule(String key, String tagPath, Comparator<NBTBase> comparator);

    /**
     * Registers a sorting rule for NBT tags
     *
     * @param tagPath      path of the nbt tag. Separate sub tags with '/'
     * @param expectedType the expected NBT tag id. Will be automatically compared
     * @see net.minecraftforge.common.util.Constants.NBT for expectedType
     */
    void registerNbtSortingRule(String key, String tagPath, int expectedType);

    /**
     * Registers a sorting rule for NBT tags
     *
     * @param tagPath      path of the nbt tag. Separate sub tags with '/'
     * @param expectedType the expected NBT tag id
     * @param comparator   comparator of the type converted by converter
     * @param converter    converts the tag found at the tagPath for the comparator
     * @see net.minecraftforge.common.util.Constants.NBT for expectedType
     */
    <T> void registerNbtSortingRule(String key, String tagPath, int expectedType, Comparator<T> comparator, Function<NBTBase, T> converter);
}
