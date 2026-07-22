package me.darrionat.pluginlib.utils;

import me.darrionat.pluginlib.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * A utility to check if a {@link Plugin} has an update available on {@code https://spigotmc.org}.
 */
public class SpigotMCUpdateHandler {
    /**
     * The current version of the plugin
     */
    private final String currentVersion;
    /**
     * The project id on SpigotMC
     */
    private final int resourceId;
    /**
     * The URL that will be used to access the SpigotMC API
     */
    private URL spigotAPI;

    /**
     * Creates a new {@link SpigotMCUpdateHandler}.
     *
     * @param plugin     The plugin that will be used to compare versions.
     * @param resourceId The project id of the resource on SpigotMC.
     */
    public SpigotMCUpdateHandler(Plugin plugin, int resourceId) {
        this.currentVersion = plugin.getPluginMeta().getVersion();
        this.resourceId = resourceId;
        try {
            this.spigotAPI = URI.create("https://api.spigotmc.org/legacy/update.php?resource=%d".formatted(resourceId)).toURL();
        } catch (IllegalArgumentException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the URL of the resource's page on SpigotMC
     *
     * @return Returns the URL of the resource's page on SpigotMC.
     */
    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/%d".formatted(resourceId);
    }

    /**
     * Checks to see if there is an update available for the SpigotMC resource.
     *
     * @return Returns {@code true} if an update is available.
     */
    public boolean updateAvailable() {
        try {
            return !currentVersion.equals(getLatestVersion());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches the latest version of the resource.
     *
     * @return Returns the latest version of the SpigotMC resource.
     * @throws IOException if an I/O Exception occurs.
     */
    public String getLatestVersion() throws IOException {
        URLConnection con = spigotAPI.openConnection();
        return new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
    }
}