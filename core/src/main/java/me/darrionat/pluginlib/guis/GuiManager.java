package me.darrionat.pluginlib.guis;

import me.darrionat.pluginlib.Plugin;
import me.darrionat.pluginlib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a {@link List} of all {@link Gui}s within a {@link Plugin} and determines the outcome of an {@link
 * InventoryClickEvent}.
 */
public class GuiManager implements GuiHandler, Listener {
    private final HashMap<String, Gui> REGISTERED_GUIS = new HashMap<>();

    public GuiManager(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * {@inheritDoc}
     */
    public void registerGui(Gui gui) {
        REGISTERED_GUIS.put(gui.getName(), gui);
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterGui(Gui gui) {
        REGISTERED_GUIS.remove(gui.getName());
    }

    /**
     * {@inheritDoc}
     */
    public void openGui(Player p, Gui gui) {
        p.openInventory(gui.getInventory(p));
    }

    /**
     * Determines what to do when an {@link InventoryClickEvent} is ran.
     *
     * @param e The Event that is passed.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || !e.getInventory().equals(e.getClickedInventory()))
            return;

        String title = Utils.getPlaintextTitle(e);
        Gui clickedGui = REGISTERED_GUIS.get(title);

        if (clickedGui == null)
            return;

        if (!clickedGui.allowsClick())
            e.setCancelled(true);

        clickedGui.clicked((Player) e.getWhoClicked(), e.getSlot(), e.getClick());
    }

    /**
     * Stops all animations when an {@link AnimatedGui} is closed.
     *
     * @param e The Event that is passed.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        String title = Utils.getPlaintextTitle(e);
        Gui closedGui = REGISTERED_GUIS.get(title);

        // null check + animation check
        if (!(closedGui instanceof AnimatedGui animated))
            return;

        // Stops all animations
        animated.stopAnimations();
    }
}