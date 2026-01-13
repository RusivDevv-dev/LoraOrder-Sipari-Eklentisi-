package com.lora.loraorder.managers;

import com.lora.loraorder.LoraOrder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CategoryManager {
    
    private final LoraOrder plugin;
    private FileConfiguration farmsConfig;
    private FileConfiguration minerConfig;
    private FileConfiguration blocksConfig;
    
    public CategoryManager(LoraOrder plugin) {
        this.plugin = plugin;
        loadCategories();
    }
    
    private void loadCategories() {
        File categoriesFolder = new File(plugin.getDataFolder(), "categories");
        if (!categoriesFolder.exists()) {
            categoriesFolder.mkdirs();
        }
        
        // Tarım kategorisi
        File farmsFile = new File(categoriesFolder, "farms.yml");
        if (!farmsFile.exists()) {
            plugin.saveResource("categories/farms.yml", false);
        }
        farmsConfig = YamlConfiguration.loadConfiguration(farmsFile);
        
        // Maden kategorisi
        File minerFile = new File(categoriesFolder, "miner.yml");
        if (!minerFile.exists()) {
            plugin.saveResource("categories/miner.yml", false);
        }
        minerConfig = YamlConfiguration.loadConfiguration(minerFile);
        
        // Blok kategorisi
        File blocksFile = new File(categoriesFolder, "blocks.yml");
        if (!blocksFile.exists()) {
            plugin.saveResource("categories/blocks.yml", false);
        }
        blocksConfig = YamlConfiguration.loadConfiguration(blocksFile);
    }
    
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("farms");
        categories.add("miner");
        categories.add("blocks");
        return categories;
    }
    
    public List<Material> getCategoryItems(String category) {
        FileConfiguration config = getCategoryConfig(category);
        List<Material> items = new ArrayList<>();
        
        if (config != null && config.contains("items")) {
            for (String itemStr : config.getStringList("items")) {
                try {
                    Material material = Material.valueOf(itemStr.toUpperCase());
                    items.add(material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz materyal: " + itemStr);
                }
            }
        }
        
        return items;
    }
    
    public ItemStack getCategoryIcon(String category) {
        FileConfiguration config = getCategoryConfig(category);
        
        Material material = Material.CHEST;
        String name = "&e" + category.toUpperCase();
        
        if (config != null) {
            try {
                material = Material.valueOf(config.getString("icon.material", "CHEST"));
            } catch (IllegalArgumentException ignored) {}
            name = config.getString("icon.name", name);
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            
            if (config != null && config.contains("icon.lore")) {
                List<String> lore = new ArrayList<>();
                for (String line : config.getStringList("icon.lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(lore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private FileConfiguration getCategoryConfig(String category) {
        switch (category.toLowerCase()) {
            case "farms":
                return farmsConfig;
            case "miner":
                return minerConfig;
            case "blocks":
                return blocksConfig;
            default:
                return null;
        }
    }
    
    public void reload() {
        loadCategories();
    }
}
