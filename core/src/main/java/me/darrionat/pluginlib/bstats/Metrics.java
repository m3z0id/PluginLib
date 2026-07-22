package me.darrionat.pluginlib.bstats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.darrionat.pluginlib.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

/**
 * bStats collects some data for plugin authors.
 * <p>
 * Check out <a href="https://bStats.org/">bStats</a> to learn more about bStats!
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Metrics {

    static {
        try {
            B_STATS_URL = new URI("https://bStats.org/submitData/bukkit").toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // You can use the property to disable the check in your test environment
        if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
            // Maven's Relocate is clever and changes strings, too. So we have to use this little "trick" ... :D
            final String defaultPackage = new String(
                    new byte[]{'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's', '.', 'b', 'u', 'k', 'k', 'i', 't'});
            final String examplePackage = new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
            // We want to make sure nobody just copy & pastes the example and use the wrong package names
            if (Metrics.class.getPackage().getName().equals(defaultPackage) || Metrics.class.getPackage().getName().equals(examplePackage)) {
                throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
            }
        }
    }

    // The version of this bStats class
    public static final int B_STATS_VERSION = 1;

    // The url to which the data is sent
    private static final URL B_STATS_URL;

    // Is bStats enabled on this server?
    private final boolean enabled;

    // Should failed requests be logged?
    private static boolean logFailedRequests;

    // Should the sent data be logged?
    private static boolean logSentData;

    // Should the response text be logged?
    private static boolean logResponseStatusText;

    // The uuid of the server
    private static UUID serverUUID;

    // The plugin
    private final Plugin plugin;

    // The plugin id
    private final int pluginId;

    // A list with all custom charts
    private final List<CustomChart> charts = new ArrayList<>();

    /**
     * Class constructor.
     *
     * @param plugin   The plugin which stats should be submitted.
     * @param pluginId The id of the plugin.
     *                 It can be found at <a href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public Metrics(Plugin plugin, int pluginId) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        this.plugin = plugin;
        this.pluginId = pluginId;

        // Get the config file
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Check if the config file exists
        if (!config.isSet("serverUuid")) {

            // Add default values
            config.addDefault("enabled", true);
            // Every server gets it's unique random id.
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            // Should failed request be logged?
            config.addDefault("logFailedRequests", false);
            // Should the sent data be logged?
            config.addDefault("logSentData", false);
            // Should the response text be logged?
            config.addDefault("logResponseStatusText", false);

            // Inform the server owners about bStats
            config.options().setHeader(List.of(
                            "bStats collects some data for plugin authors like how many servers are using their plugins.",
                            "To honor their work, you should not disable it.",
                            "This has nearly no effect on the server performance!",
                            "Check out https://bStats.org/ to learn more :)"
                    )).copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) {
            }
        }

        // Load the data
        enabled = config.getBoolean("enabled", true);
        serverUUID = UUID.fromString(Objects.requireNonNull(config.getString("serverUuid")));
        logFailedRequests = config.getBoolean("logFailedRequests", false);
        logSentData = config.getBoolean("logSentData", false);
        logResponseStatusText = config.getBoolean("logResponseStatusText", false);

        if (enabled) {
            boolean found = false;
            // Search for all other bStats Metrics classes to see if we are the first one
            for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
                try {
                    service.getField("B_STATS_VERSION"); // Our identifier :)
                    found = true; // We aren't the first
                    break;
                } catch (NoSuchFieldException ignored) {
                }
            }
            // Register our service
            Bukkit.getServicesManager().register(Metrics.class, this, plugin, ServicePriority.Normal);
            if (!found) {
                // We are the first!
                startSubmitting();
                plugin.log("Metrics enabled");
            }
        }
    }

    /**
     * Checks if bStats is enabled.
     *
     * @return Whether bStats is enabled or not.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(CustomChart chart) {
        if (chart == null) {
            throw new IllegalArgumentException("Chart cannot be null!");
        }
        charts.add(chart);
    }

    /**
     * Starts the Scheduler which submits our data every 30 minutes.
     */
    private void startSubmitting() {
        // Many servers tend to restart at a fixed time at xx:00 which causes an uneven distribution of requests on the
        // bStats backend. To circumvent this problem, we introduce some randomness into the initial and second delay.
        // WARNING: You must not modify and part of this Metrics class, including the submit delay or frequency!
        // WARNING: Modifying this code will get your plugin banned on bStats. Just don't do it!
        // WARNING: Values below are in Minecraft ticks (1/20th of a second)
        long initialDelay = 20L * 60L * (long) (3L + Math.random() * 3L);
        long secondDelay = 20L * 60L * (long) (Math.random() * 30L);

        long thirtyMinuteDelay = 20L * 60L * 30L;

        // Nevertheless we want our code to run in the Bukkit main thread, so we have to use the Bukkit scheduler
        // Don't be afraid! The connection to the bStats server is still async, only the stats collection is sync ;)
        Bukkit.getScheduler().runTaskTimer(plugin, this::submitData, initialDelay + secondDelay, thirtyMinuteDelay);
    }

    /**
     * Gets the plugin specific data.
     * This method is called using Reflection.
     *
     * @return The plugin specific data.
     */
    public JsonObject getPluginData() {
        JsonObject data = new JsonObject();

        String pluginName = plugin.getPluginMeta().getName();
        String pluginVersion = plugin.getPluginMeta().getVersion();

        data.addProperty("pluginName", pluginName); // Append the name of the plugin
        data.addProperty("id", pluginId); // Append the id of the plugin
        data.addProperty("pluginVersion", pluginVersion); // Append the version of the plugin
        JsonArray customCharts = new JsonArray();
        for (CustomChart customChart : charts) {
            // Add the data of the custom charts
            JsonObject chart = customChart.getRequestJsonObject();
            if (chart == null) { // If the chart is null, we skip it
                continue;
            }
            customCharts.add(chart);
        }
        data.add("customCharts", customCharts);

        return data;
    }

    /**
     * Gets the server specific data.
     *
     * @return The server specific data.
     */
    private JsonObject getServerData() {
        // Minecraft specific data
        int playerAmount = Bukkit.getOnlinePlayers().size();
        int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
        String bukkitVersion = Bukkit.getVersion();
        String bukkitName = Bukkit.getName();

        // OS/Java specific data
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        int coreCount = Runtime.getRuntime().availableProcessors();

        JsonObject data = new JsonObject();

        data.addProperty("serverUUID", serverUUID.toString());

        data.addProperty("playerAmount", playerAmount);
        data.addProperty("onlineMode", onlineMode);
        data.addProperty("bukkitVersion", bukkitVersion);
        data.addProperty("bukkitName", bukkitName);

        data.addProperty("javaVersion", javaVersion);
        data.addProperty("osName", osName);
        data.addProperty("osArch", osArch);
        data.addProperty("osVersion", osVersion);
        data.addProperty("coreCount", coreCount);

        return data;
    }

    /**
     * Collects the data and sends it afterwards.
     */
    private void submitData() {
        final JsonObject data = getServerData();

        JsonArray pluginData = new JsonArray();
        // Search for all other bStats Metrics classes to get their plugin data
        for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            try {
                service.getField("B_STATS_VERSION"); // Our identifier :)

                for (RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(service)) {
                    Object plugin = provider.getService().getMethod("getPluginData").invoke(provider.getProvider());
                    if (plugin instanceof JsonObject pl) {
                        pluginData.add(pl);
                    }
                }
            } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                if (logFailedRequests) {
                    this.plugin.getLogger().log(Level.SEVERE, "Encountered unexpected exception", e);
                }
            }
        }

        data.add("plugins", pluginData);

        // Create a new thread for the connection to the bStats server
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Send the data
                sendData(plugin, data);
            } catch (Exception e) {
                // Something went wrong! :(
                if (logFailedRequests) {
                    plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of %s".formatted(plugin.getName()), e);
                }
            }
        });
    }

    /**
     * Sends the data to the bStats server.
     *
     * @param plugin Any plugin. It's just used to get a logger instance.
     * @param data   The data to send.
     * @throws Exception If the request failed.
     */
    private static void sendData(Plugin plugin, JsonObject data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null!");
        }
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalAccessException("This method must not be called from the main thread!");
        }
        if (logSentData) {
            plugin.getLogger().info("Sending data to bStats: %s".formatted(data.toString()));
        }
        HttpsURLConnection connection = (HttpsURLConnection) B_STATS_URL.openConnection();

        // Compress the data to save bandwidth
        byte[] compressedData = compress(data.toString());

        // Add headers
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip our request
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json"); // We send our data in JSON format
        connection.setRequestProperty("User-Agent", "MC-Server/%d".formatted(B_STATS_VERSION));

        // Send data
        connection.setDoOutput(true);
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(compressedData);
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        }

        if (logResponseStatusText) {
            plugin.getLogger().info("Sent data to bStats and received response: %s".formatted(builder));
        }
    }

    /**
     * Gzips the given String.
     *
     * @param str The string to gzip.
     * @return The gzipped String.
     * @throws IOException If the compression failed.
     */
    private static byte[] compress(final String str) throws IOException {
        if (str == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
        }
        return outputStream.toByteArray();
    }

    /**
     * Represents a custom chart.
     */
    public static abstract class CustomChart {

        // The id of the chart
        final String chartId;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         */
        CustomChart(@NotNull String chartId) {
            if (chartId.isEmpty()) {
                throw new IllegalArgumentException("ChartId cannot be null or empty!");
            }
            this.chartId = chartId;
        }

        private JsonObject getRequestJsonObject() {
            JsonObject chart = new JsonObject();
            chart.addProperty("chartId", chartId);
            try {
                JsonObject data = getChartData();
                if (data == null) {
                    // If the data is null we don't send the chart.
                    return null;
                }
                chart.add("data", data);
            } catch (Throwable t) {
                if (logFailedRequests) {
                    Plugin.getProject().getLogger().log(Level.WARNING, "Failed to get data for custom chart with id %s".formatted(chartId), t);
                }
                return null;
            }
            return chart;
        }

        protected abstract JsonObject getChartData();
    }

    /**
     * Represents a custom simple pie.
     */
    public static class SimplePie extends CustomChart {

        private final Supplier<String> supplier;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param supplier The supplier which is used to request the chart data.
         */
        public SimplePie(@NotNull String chartId, @NotNull Supplier<String> supplier) {
            super(chartId);
            this.supplier = supplier;
        }

        @Override
        protected JsonObject getChartData() {
            JsonObject data = new JsonObject();
            String value = supplier.get();
            if (value == null || value.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            data.addProperty("value", value);
            return data;
        }
    }

    /**
     * Represents a custom advanced pie.
     */
    public static class AdvancedPie extends CustomChart {

        private final Supplier<Map<String, @NotNull Integer>> supplier;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param supplier The supplier which is used to request the chart data.
         */
        public AdvancedPie(@NotNull String chartId, @NotNull Supplier<Map<String, @NotNull Integer>> supplier) {
            super(chartId);
            this.supplier = supplier;
        }

        @Override
        protected JsonObject getChartData() {
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            Map<String, Integer> map = supplier.get();
            if (map == null || map.isEmpty()) {
                return null;
            }

            map.entrySet().stream()
                    .filter(entry -> entry.getValue() != 0)
                    .forEach(entry -> {values.addProperty(entry.getKey(), entry.getValue());});

            if (values.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            data.add("values", values);
            return data;
        }
    }

    /**
     * Represents a custom drilldown pie.
     */
    public static class DrilldownPie extends CustomChart {

        private final Supplier<Map<String, @NotNull Map<String, @NotNull Integer>>> supplier;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param supplier The supplier which is used to request the chart data.
         */
        public DrilldownPie(@NotNull String chartId, @NotNull Supplier<Map<String, @NotNull Map<String, @NotNull Integer>>> supplier) {
            super(chartId);
            this.supplier = supplier;
        }

        @Override
        public JsonObject getChartData() {
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            Map<String, Map<String, Integer>> map = supplier.get();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }


            map.forEach((key, value) -> {
                JsonObject subval = new JsonObject();
                value.forEach(subval::addProperty);
                values.add(key, subval);
            });
            if(values.isEmpty()) {
                // Null = skip the chart
                return null;
            }

            data.add("values", values);
            return data;
        }
    }

    /**
     * Represents a custom single line chart.
     */
    public static class SingleLineChart extends CustomChart {

        private final Supplier<@NotNull Integer> supplier;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param supplier The supplier which is used to request the chart data.
         */
        public SingleLineChart(String chartId, @NotNull Supplier<@NotNull Integer> supplier) {
            super(chartId);
            this.supplier = supplier;
        }

        @Override
        protected JsonObject getChartData() {
            JsonObject data = new JsonObject();
            int value = supplier.get();
            if (value == 0) {
                // Null = skip the chart
                return null;
            }
            data.addProperty("value", value);
            return data;
        }

    }

    /**
     * Represents a custom multi line chart.
     */
    public static class MultiLineChart extends CustomChart {

        private final Supplier<Map<String, @NotNull Integer>> supplier;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param supplier The supplier which is used to request the chart data.
         */
        public MultiLineChart(String chartId, @NotNull Supplier<Map<String, @NotNull Integer>> supplier) {
            super(chartId);
            this.supplier = supplier;
        }

        @Override
        protected JsonObject getChartData() {
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            Map<String, Integer> map = supplier.get();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            map.entrySet().stream()
                .filter(entry -> entry.getValue() != 0)
                .forEach(entry -> {values.addProperty(entry.getKey(), entry.getValue());});

            if (values.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            data.add("values", values);
            return data;
        }

    }

    /**
     * Represents a custom simple bar chart.
     */
    public static class SimpleBarChart extends CustomChart {

        private final Supplier<Map<String, @NotNull Integer>> supplier;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param supplier The supplier which is used to request the chart data.
         */
        public SimpleBarChart(String chartId, @NotNull Supplier<Map<String, @NotNull Integer>> supplier) {
            super(chartId);
            this.supplier = supplier;
        }

        @Override
        protected JsonObject getChartData() {
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            Map<String, Integer> map = supplier.get();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }

            map.forEach((k, v) -> {
                JsonArray categoryValues = new JsonArray();
                categoryValues.add(v);
                values.add(k, categoryValues);
            });

            data.add("values", values);
            return data;
        }

    }

    /**
     * Represents a custom advanced bar chart.
     */
    public static class AdvancedBarChart extends CustomChart {

        private final Supplier<Map<String, int[]>> supplier;

        /**
         * Class constructor.
         *
         * @param chartId  The id of the chart.
         * @param supplier The supplier which is used to request the chart data.
         */
        public AdvancedBarChart(String chartId, @NotNull Supplier<Map<String, int[]>> supplier) {
            super(chartId);
            this.supplier = supplier;
        }

        @Override
        protected JsonObject getChartData() {
            JsonObject data = new JsonObject();
            JsonObject values = new JsonObject();
            Map<String, int[]> map = supplier.get();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }

            map.entrySet().stream()
                .filter(entry -> entry.getValue().length != 0)
                .forEach(entry -> {
                    JsonArray categoryValues = new JsonArray();
                    Arrays.stream(entry.getValue()).forEach(categoryValues::add);
                    values.add(entry.getKey(), categoryValues);
                });

            if (values.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            data.add("values", values);
            return data;
        }
    }
}