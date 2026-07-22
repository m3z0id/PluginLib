package me.darrionat.pluginlib.enchantments;

import io.papermc.paper.enchantments.EnchantmentRarity;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import me.darrionat.pluginlib.Plugin;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"removal", "UnstableApiUsage"})
public class EnchantmentBuilder {

    /**
     * The translation key used for localization of the enchantment.
     */
    private String translationKey = null;

    /**
     * The key string used to identify the enchantment item's namespace.
     */
    private String itemKey = null;

    /**
     * The unique display name of the enchantment.
     */
    private String name = null;

    /**
     * The description component detailing the enchantment's behavior or effects.
     */
    private Component description = Component.empty();

    /**
     * The maximum level this enchantment can naturally reach.
     */
    private int maxLevel = 1;

    /**
     * The minimum (starting) level of this enchantment.
     */
    private int minLevel = 1;

    /**
     * The item target category for this enchantment.
     */
    private EnchantmentTarget target = EnchantmentTarget.ALL;

    /**
     * Whether this enchantment is considered a treasure enchantment.
     */
    private boolean treasure = false;

    /**
     * Whether this enchantment is a curse.
     */
    private boolean cursed = false;

    /**
     * Whether this enchantment can be traded via villager trades.
     */
    private boolean tradeable = false;

    /**
     * Whether this enchantment can be found via loot tables or enchanting tables.
     */
    private boolean discoverable = false;

    /**
     * A set of enchantments that cannot coexist with this enchantment.
     */
    private final HashSet<Enchantment> enchantmentBlacklist = new HashSet<>();

    /**
     * A set of item types explicitly allowed to be enchanted with this enchantment.
     */
    private final HashSet<ItemType> itemWhitelist = new HashSet<>();

    /**
     * The base display name component used before level suffixes are appended.
     */
    private Component baseDisplayName = null;

    /**
     * The minimum modified enchantment cost requirement.
     */
    private int minModifiedCost = 0;

    /**
     * The maximum modified enchantment cost requirement.
     */
    private int maxModifiedCost = 0;

    /**
     * The base repair cost required when applying this enchantment via an anvil.
     */
    private int anvilCost = 0;

    /**
     * The rarity tier of the enchantment.
     */
    private EnchantmentRarity rarity = EnchantmentRarity.COMMON;

    /**
     * A map storing damage increase multipliers scaled per level for specific entity categories.
     */
    private final HashMap<EntityCategory, Float> categoryIncreaseMultiplierMap = new HashMap<>();

    /**
     * A map storing damage increase multipliers scaled per level for specific entity types.
     */
    private final HashMap<EntityType, Float> typeIncreaseMultiplierMap = new HashMap<>();

    /**
     * A set of equipment slot groups where this enchantment's effects are actively applied.
     */
    private final HashSet<EquipmentSlotGroup> activeEquipmentSlots = new HashSet<>();

    /**
     * The weight factor determining how frequently this enchantment appears relative to others.
     */
    private int weight = 0;

    /**
     * Sets the translation key for localizing this enchantment. This field is required to be set.
     *
     * @param translationKey the translation key string
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withTranslationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }

    /**
     * Sets the internal name of this enchantment. This field is required.
     *
     * @param name the unique name string
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the item key used for registering the enchantment's {@link NamespacedKey}. This field is required.
     *
     * @param itemKey the item key string
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withItemKey(String itemKey) {
        this.itemKey = itemKey;
        return this;
    }

    /**
     * Sets the description component for this enchantment.
     *
     * @param description the description as a {@link Component}
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withDescription(Component description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the maximum level this enchantment can reach.
     *
     * @param maxLevel the maximum level
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    /**
     * Sets the starting level for this enchantment.
     *
     * @param minLevel the starting level
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withMinLevel(int minLevel) {
        this.minLevel = minLevel;
        return this;
    }

    /**
     * Sets the targeted item category for this enchantment.
     *
     * @param target the {@link EnchantmentTarget} category
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withTarget(EnchantmentTarget target) {
        this.target = target;
        return this;
    }

    /**
     * Marks this enchantment as a treasure enchantment, preventing it from appearing in standard enchantment tables.
     *
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder treasure() {
        this.treasure = true;
        return this;
    }

    /**
     * Marks this enchantment as a curse.
     *
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder curse() {
        this.cursed = true;
        return this;
    }

    /**
     * Marks this enchantment as tradeable via villager trades.
     *
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder tradeable() {
        this.tradeable = true;
        return this;
    }

    /**
     * Marks this enchantment as discoverable in world loot or standard enchanting methods.
     *
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder discoverable() {
        this.discoverable = true;
        return this;
    }

    /**
     * Adds conflicting enchantments that cannot be applied alongside this enchantment.
     *
     * @param conflictEnchantments the conflicting {@link Enchantment} instances
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withConflicts(Enchantment... conflictEnchantments) {
        this.enchantmentBlacklist.addAll(Arrays.asList(conflictEnchantments));
        return this;
    }

    /**
     * Whitelists specific item types that can receive this enchantment.
     *
     * @param items the allowed {@link ItemType}s
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder canEnchantItems(ItemType... items) {
        itemWhitelist.addAll(Arrays.asList(items));
        return this;
    }

    /**
     * Sets the base display name component for this enchantment.
     *
     * @param displayName the base display name {@link Component}
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withDisplayName(Component displayName) {
        this.baseDisplayName = displayName;
        return this;
    }

    /**
     * Sets the minimum modified cost requirement for enchanting.
     *
     * @param cost the minimum cost requirement
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withMinModifiedCost(int cost) {
        this.minModifiedCost = cost;
        return this;
    }

    /**
     * Sets the maximum modified cost limit for enchanting.
     *
     * @param cost the maximum cost limit
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withMaxModifiedCost(int cost) {
        this.maxModifiedCost = cost;
        return this;
    }

    /**
     * Sets the base level cost added during anvil operations.
     *
     * @param cost the anvil cost
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withAnvilCost(int cost) {
        this.anvilCost = cost;
        return this;
    }

    /**
     * Sets the rarity tier for this enchantment.
     *
     * @param rarity the {@link EnchantmentRarity}
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withRarity(EnchantmentRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    /**
     * Adds a damage increase multiplier per level targeting a specific entity category.
     *
     * @param damageIncrease the damage multiplier per level
     * @param category the target {@link EntityCategory}
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withDamageIncreaseOn(float damageIncrease, EntityCategory category) {
        categoryIncreaseMultiplierMap.put(category, damageIncrease);
        return this;
    }

    /**
     * Adds a damage increase multiplier per level targeting a specific entity type.
     *
     * @param damageIncrease the damage multiplier per level
     * @param type the target {@link EntityType}
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withDamageIncreaseOn(float damageIncrease, EntityType type) {
        typeIncreaseMultiplierMap.put(type, damageIncrease);
        return this;
    }

    /**
     * Adds active equipment slot groups where this enchantment's effects will apply.
     *
     * @param activeSlots the active {@link EquipmentSlotGroup}s
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withActiveGroupSlots(EquipmentSlotGroup... activeSlots) {
        activeEquipmentSlots.addAll(Arrays.asList(activeSlots));
        return this;
    }

    /**
     * Sets the weight determining the probability of this enchantment appearing.
     *
     * @param weight the rarity weight value
     * @return Returns the {@link EnchantmentBuilder}.
     */
    public EnchantmentBuilder withWeight(int weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Builds the {@link Enchantment} object being built.
     *
     * @return Returns the {@link Enchantment} specified.
     * @throws IllegalStateException if required fields are unset.
     */
    public Enchantment build() throws IllegalStateException {
        if(translationKey == null) throw new IllegalStateException("translationKey is null");
        if(name == null) throw new IllegalStateException("name is null");
        if(itemKey == null) throw new IllegalStateException("itemKey is null");


        return new Enchantment() {
            @Override
            public @NotNull String getName() {
                return name;
            }

            @Override
            public int getMaxLevel() {
                return maxLevel;
            }

            @Override
            public int getStartLevel() {
                return minLevel;
            }

            @Override
            public @NotNull EnchantmentTarget getItemTarget() {
                return target;
            }

            @Override
            public boolean isTreasure() {
                return treasure;
            }

            @Override
            public boolean isCursed() {
                return cursed;
            }

            @Override
            public boolean conflictsWith(@NotNull Enchantment other) {
                return enchantmentBlacklist.contains(other);
            }

            @Override
            public boolean canEnchantItem(@NotNull ItemStack item) {
                if(itemWhitelist.isEmpty()) return true;

                return itemWhitelist.contains(item);
            }

            @Override
            public @NotNull Component displayName(int level) {
                return baseDisplayName.append(Component.text(" "), Component.text(level));
            }

            @Override
            public boolean isTradeable() {
                return tradeable;
            }

            @Override
            public boolean isDiscoverable() {
                return discoverable;
            }

            @Override
            public int getMinModifiedCost(int level) {
                return minModifiedCost;
            }

            @Override
            public int getMaxModifiedCost(int level) {
                return maxModifiedCost;
            }

            @Override
            public int getAnvilCost() {
                return anvilCost;
            }

            @Override
            public @NotNull EnchantmentRarity getRarity() {
                return rarity;
            }

            @Override
            public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
                return level * categoryIncreaseMultiplierMap.getOrDefault(entityCategory, 0.0F);
            }

            @Override
            public float getDamageIncrease(int level, @NotNull EntityType entityType) {
                return level * typeIncreaseMultiplierMap.getOrDefault(entityType, 0.0F);
            }

            @Override
            public @NotNull Set<EquipmentSlotGroup> getActiveSlotGroups() {
                return activeEquipmentSlots;
            }

            @Override
            public @NotNull Component description() {
                return description;
            }

            @Override
            public @NotNull RegistryKeySet<ItemType> getSupportedItems() {
                return RegistrySet.keySetFromValues(RegistryKey.ITEM, itemWhitelist);
            }

            @Override
            public @Nullable RegistryKeySet<ItemType> getPrimaryItems() {
                return null;
            }

            @Override
            public int getWeight() {
                return weight;
            }

            @Override
            public @NotNull RegistryKeySet<Enchantment> getExclusiveWith() {
                return RegistrySet.keySetFromValues(RegistryKey.ENCHANTMENT, enchantmentBlacklist);
            }

            @Override
            public @NotNull String translationKey() {
                return translationKey;
            }

            @Override
            public @NotNull NamespacedKey getKey() {
                return new NamespacedKey(Plugin.getProject().namespace(), itemKey);
            }

            @Override
            public @NotNull String getTranslationKey() {
                return translationKey;
            }
        };
    }
}