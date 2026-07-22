package me.darrionat.pluginlib.guis;

import me.darrionat.pluginlib.Plugin;
import me.darrionat.pluginlib.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Represents an {@link Inventory} that is displayed to a player.
 */
public abstract class Gui {
    /**
     * The plugin that this belongs to.
     */
    protected final Plugin plugin;
    /**
     * The inventory to display.
     */
    protected final Inventory inv;
    /**
     * The title of the inventory.
     */
    protected final Component name;
    /**
     * The size of the inventory.
     */
    protected final int size;
    /**
     * A map of players and the inventories they are shown.
     */
    private final HashMap<Player, Inventory> byPlayer = new HashMap<>();
    /**
     * Determines if a {@link Player} who clicks within the {@link Gui} is able to take items out of the displayed
     * inventory.
     */
    private boolean allowClick = false;

    /**
     * Creates a new {@link Gui}.
     *
     * @param name   The title of the gui.
     * @param rows   The amount of rows the {@link Inventory} will have.
     */
    public Gui(Component name, int rows) throws IllegalArgumentException {
        this.plugin = Plugin.getProject();
        this.size = rows * 9;

        String nameStr = Utils.getComponentPlaintext(name);

        if (nameStr.length() > 32)
            throw new IllegalArgumentException("%s is longer than 32 characters".formatted(nameStr));
        this.name = name;

        inv = Bukkit.createInventory(null, size, name);
        plugin.getGuiHandler().registerGui(this);
    }

    /**
     * Gets the title of the {@link Inventory}.
     *
     * @return The name of the {@link Gui}.
     */
    public String getName() {
        return Utils.getComponentPlaintext(name);
    }

    /**
     * Determines if a {@link Player} who clicks within the {@link Gui} is able to take items out of the displayed
     * inventory. Set to {@code false} by default.
     * <p>
     * Recommended: {@code false}
     *
     * @param allow Whether to allow clicking or not.
     */
    public void allowClick(boolean allow) {
        this.allowClick = allow;
    }

    /**
     * Checks to see if a {@link Player} is allowed to click within the {@link Inventory}.
     *
     * @return Returns {@code true} if a player is allowed take items from the {@link Gui}.
     */
    public boolean allowsClick() {
        return allowClick;
    }

    /**
     * Gets a copy of the Inventory
     *
     * @param p The player the inventory is being displayed to.
     * @return Returns a copy of the Inventory.
     */
    public Inventory getInventory(Player p) {
        Inventory toReturn = Bukkit.createInventory(null, size, name);
        if (byPlayer.containsKey(p)) {
            // Update player's inventory
            toReturn = byPlayer.get(p);
        } else {
            // Set player's inventory
            byPlayer.put(p, toReturn);
        }
        getContents(p);
        toReturn.setContents(inv.getContents());
        return toReturn;
    }

    /**
     * Creates an item and places it within the {@link Gui}.
     *
     * @param material   The material of the item.
     * @param amount     The amount of the item to display.
     * @param invSlot    The slot for the item to go into.
     * @param name       The display name of the item.
     * @param loreString The lines of the lore of the item.
     */
    public void createItem(Material material, int amount, int invSlot, Component name, Component... loreString) {
        List<Component> lore = new ArrayList<>(Arrays.asList(loreString));
        createItem(material, amount, invSlot, name, lore);
    }

    /**
     * Creates an item and places it within the {@link Gui}.
     *
     * @param material The material of the item.
     * @param amount   The amount of the item to display.
     * @param invSlot  The slot for the item to go into.
     * @param name     The display name of the item.
     * @param lore     The lore of the item.
     * @return Returns the item placed within the {@link Gui}.
     */
    public ItemStack createItem(Material material, int amount, int invSlot, Component name, List<Component> lore) {
        return createItem(Utils.buildItem(material, amount, name, lore), invSlot);
    }

    /**
     * Creates an item and places it directly into a player's instance of a {@link Gui}.
     *
     * @param p        The player with the open gui.
     * @param material The material of the item.
     * @param amount   The amount of the item to display.
     * @param invSlot  The slot for the item to go into.
     * @param name     The display name of the item.
     * @param lore     The lore of the item.
     * @return Returns the item placed directly into the player's {@link Gui}.
     */
    public ItemStack createItem(Player p, Material material, int amount, int invSlot, Component name, List<Component> lore) {
        return createItem(byPlayer.get(p), Utils.buildItem(material, amount, name, lore), invSlot);
    }

    /**
     * Creates an item and places it directly into a player's instance of a {@link Gui}.
     *
     * @param item    The item to place within the inventory.
     * @param p       The player with the open gui.
     * @param invSlot The slot for the item to go into.
     * @return Returns the item placed directly into the player's {@link Gui}.
     */
    public ItemStack createItem(Player p, ItemStack item, int invSlot) {
        return createItem(byPlayer.get(p), item, invSlot);
    }

    /**
     * Creates an item and places it within the {@link Gui}.
     *
     * @param item    The item to place within the inventory.
     * @param invSlot The slot for the item to go into.
     * @return Returns the item placed within the {@link Gui}.
     */
    public ItemStack createItem(ItemStack item, int invSlot) {
        return createItem(this.inv, item, invSlot);
    }

    /**
     * Creates an item and places it within the {@link Gui}.
     *
     * @param inv     The inventory to place the item into.
     * @param item    The item to place within the inventory.
     * @param invSlot The slot for the item to go into.
     * @return Returns the item placed within the {@link Gui}.
     */
    private ItemStack createItem(Inventory inv, ItemStack item, int invSlot) {
        inv.setItem(invSlot, item);
        return item;
    }

    /**
     * Gets the contents of what is within the {@link Inventory}.
     *
     * @param p The player that is opening the {@link Gui}.
     */
    protected abstract void getContents(Player p);

    /**
     * Ran when a {@link Player} has clicked within the {@link Inventory}.
     *
     * @param p         The Player who clicked.
     * @param slot      The slot the player clicked.
     * @param clickType The type of click done by the player.
     */
    public abstract void clicked(Player p, int slot, ClickType clickType);
}