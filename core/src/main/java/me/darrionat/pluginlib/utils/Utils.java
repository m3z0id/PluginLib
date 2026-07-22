package me.darrionat.pluginlib.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Small useful methods that don't belong anywhere else.
 */
public class Utils {
    /**
     * Creates an item from given information.
     *
     * @param material   The material of the item.
     * @param amount     The amount of the item.
     * @param name       The name of the item. If {@code null}, the item's display name will be a single space.
     * @param loreString The lore of the item represented by multiple strings. If {@code null}, item's lore will be
     *                   empty.
     * @return The created item.
     */
    public static ItemStack buildItem(Material material, int amount, Component name, Component... loreString) {
        List<Component> lore = new ArrayList<>(Arrays.asList(loreString));
        return buildItem(material, amount, name, lore);
    }

    /**
     * Creates an item from given information.
     *
     * @param material The material of the item.
     * @param amount   The amount of the item.
     * @param name     The name of the item. If {@code null}, the item's display name will be a single space.
     * @param lore     The lore of the item. If {@code null}, item's lore will be empty.
     * @return The created item.
     */
    public static ItemStack buildItem(Material material, int amount, Component name, List<Component> lore) {
        ItemStack item = Objects.requireNonNull(material.asItemType()).createItemStack();
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        if (name == null || getComponentPlaintext(name).isEmpty())
            name = Component.text(" ");

        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Extracts the plaintext inside a component
     *
     * @param component Component to extract the plaintext from.
     * @return Extracted plaintext
     */
    public static String getComponentPlaintext(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Extracts a container's title in an event as plaintext
     *
     * @param event Inventory event to extract the title from
     * @return Title plaintext
     */
    public static <T extends InventoryEvent> String getPlaintextTitle(T event) {
        return getComponentPlaintext(event.getView().title());
    }
}