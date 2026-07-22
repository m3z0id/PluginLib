package me.darrionat.pluginlib;

import me.darrionat.pluginlib.bstats.Metrics;
import me.darrionat.pluginlib.guis.GuiHandler;
import me.darrionat.pluginlib.guis.GuiManager;
import me.darrionat.pluginlib.utils.SpigotMCUpdateHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a {@link JavaPlugin}.
 */
public abstract class Plugin extends JavaPlugin implements IPlugin {
    private static Plugin instance;
    private GuiHandler guiHandler;
    protected Metrics metrics;

    /**
     * Gets the current project of the {@link Plugin}.
     *
     * @return Returns an instance of this {@link Plugin}.
     */
    public static Plugin getProject() {
        return instance;
    }

    /**
     * Ran when the plugin is enabled. This method cannot be overridden by a subclass.
     *
     * @see Plugin#initPlugin()
     */
    @Override
    public final void onEnable() {
        instance = this;
        guiHandler = new GuiManager(this);
        initPlugin();
    }

    /**
     * {@inheritDoc}
     */
    public final GuiHandler getGuiHandler() {
        return guiHandler;
    }

    /**
     * Gets the resource id of this project for SpigotMC.
     *
     * @return The project id of the resource on SpigotMC.
     */
    public abstract int getSpigotResourceId();

    /**
     * Gets the resource id on bStats.
     *
     * @return The project id of the resource on bStats
     */
    public abstract int getbStatsResourceId();

    /**
     * Enables bStats Metrics. Requires that {@code getbStatsResourceId} returns a valid resource id.
     */
    public void enableMetrics() {
        this.metrics = new Metrics(this, getbStatsResourceId());
    }

    /**
     * Creates a new {@link SpigotMCUpdateHandler}.
     *
     * @return Returns a {@link SpigotMCUpdateHandler} for this plugin.
     */
    public final SpigotMCUpdateHandler buildUpdateChecker() {
        return new SpigotMCUpdateHandler(this, getSpigotResourceId());
    }

    /**
     * {@inheritDoc}
     */
    public void log(String s) {
        this.getLogger().info(s);
    }
}