package me.darrionat.pluginlib.prompts;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.darrionat.pluginlib.Plugin;
import me.darrionat.pluginlib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a {@link Listener} that handles chat prompts.
 */
public class ChatPromptListener implements Listener {
    private static final HashMap<UUID, Task> ACTIVE_TASKS = new HashMap<>();
    private static boolean registered = false;
    private final Plugin plugin;

    public ChatPromptListener(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        registered = true;
    }

    /**
     * Adds an incomplete task to the list of active tasks.
     *
     * @param task The task to add.
     * @throws IllegalStateException Thrown when the task argument is completed.
     */
    public static void add(Task task) {
        if (!registered)
            new ChatPromptListener(Plugin.getProject());
        if (task.complete())
            throw new IllegalStateException("A completed task cannot be added");

        // Replaces the task automatically
        ACTIVE_TASKS.put(task.getPlayer().getUniqueId(), task);
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();

        Task task = ACTIVE_TASKS.get(p.getUniqueId());
        if (task == null) return;

        e.setCancelled(true);
        String text = Utils.getComponentPlaintext(e.message());
        if (!task.valid(text)) {
            p.sendMessage(task.onFail());
            return;
        }
        if (task.complete()) {
            Bukkit.getScheduler().runTask(plugin, task::run);
            ACTIVE_TASKS.remove(task.getPlayer().getUniqueId());
        } else p.sendMessage(task.promptText());
    }
}