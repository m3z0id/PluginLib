package me.darrionat.pluginlib.schematic;

import org.bukkit.Material;

import java.util.HashMap;

/**
 * The {@code MaterialService} is a handler for all materials contained within the {@link Material} enum.
 * <p>
 * This service operates by a {@link HashMap}. The map stores all material hashes with their associated material as the
 * value for quick reverse lookup.
 *
 * @see #findMaterial(int)
 */
public class MaterialService {
    /**
     * A HashMap implementation of all {@link Material}s.
     * <p>
     * The map of {@code <Integer, Material>} allows quick reverse lookup so that an {@code Material} can be found
     * from its hash.
     */
    public static final HashMap<Integer, Material> MATERIAL_HASH_MAP = new HashMap<>();

    // Statically initializes the map
    static {
        for (Material material : Material.values()) {
            MATERIAL_HASH_MAP.put(material.hashCode(), material);
        }
    }

    /**
     * Fetches the {@code Material} that has a specific hash.
     *
     * @param hash The hash of the material data being searched.
     * @return Returns the found {@code Material} matching the given hash; {@code null} if not found.
     */
    public static Material findMaterial(int hash) {
        return MATERIAL_HASH_MAP.get(hash);
    }
}