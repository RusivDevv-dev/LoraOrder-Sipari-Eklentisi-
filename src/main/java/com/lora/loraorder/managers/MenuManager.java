package com.lora.loraorder.managers;

import com.lora.loraorder.LoraOrder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MenuManager {
    
    private final LoraOrder plugin;
    private FileConfiguration mainMenu;
    private FileConfiguration categoryMenu;
    private FileConfiguration createMenu;
    
    public MenuManager(LoraOrder plugin) {
        this.plugin = plugin;
        loadMenus();
    }
    
    private void loadMenus() {
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }
        
        // Ana menü
        File mainFile = new File(menusFolder, "main.yml");
        if (!mainFile.exists()) {
            plugin.saveResource("menus/main.yml", false);
        }
        mainMenu = YamlConfiguration.loadConfiguration(mainFile);
        
        // Kategori menüsü
        File categoryFile = new File(menusFolder, "category.yml");
        if (!categoryFile.exists()) {
            plugin.saveResource("menus/category.yml", false);
        }
        categoryMenu = YamlConfiguration.loadConfiguration(categoryFile);
        
        // Oluşturma menüsü
        File createFile = new File(menusFolder, "create.yml");
        if (!createFile.exists()) {
            plugin.saveResource("menus/create.yml", false);
        }
        createMenu = YamlConfiguration.loadConfiguration(createFile);
    }
    
    public Inventory createMainMenu() {
        String title = plugin.getMessageManager().getMessage("menu-main-title");
        int size = mainMenu.getInt("size");
        Inventory inv = Bukkit.createInventory(null, size, title);
        
        ConfigurationSection items = mainMenu.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                int slot = items.getInt(key + ".slot");
                ItemStack item = createMenuItem(items.getConfigurationSection(key));
                inv.setItem(slot, item);
            }
        }
        
        return inv;
    }
    
    public Inventory createCategoryMenu() {
        String title = plugin.getMessageManager().getMessage("menu-category-title");
        int size = categoryMenu.getInt("size");
        Inventory inv = Bukkit.createInventory(null, size, title);
        
        // Kategorileri ekle - Sabit slotlara yerleştir
        inv.setItem(11, plugin.getCategoryManager().getCategoryIcon("farms"));
        inv.setItem(13, plugin.getCategoryManager().getCategoryIcon("miner"));
        inv.setItem(15, plugin.getCategoryManager().getCategoryIcon("blocks"));
        
        // Geri butonu
        ConfigurationSection backButton = categoryMenu.getConfigurationSection("items.back");
        if (backButton != null) {
            int slot = backButton.getInt("slot");
            ItemStack item = createMenuItem(backButton);
            inv.setItem(slot, item);
        }
        
        return inv;
    }
    
    public Inventory createOrderCreationMenu() {
        String title = plugin.getMessageManager().getMessage("menu-order-creation-title");
        int size = createMenu.getInt("size");
        Inventory inv = Bukkit.createInventory(null, size, title);
        
        ConfigurationSection items = createMenu.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                int slot = items.getInt(key + ".slot");
                ItemStack item = createMenuItem(items.getConfigurationSection(key));
                inv.setItem(slot, item);
            }
        }
        
        return inv;
    }
    
    private ItemStack createMenuItem(ConfigurationSection section) {
        Material material = Material.valueOf(section.getString("material", "STONE"));
        String name = ChatColor.translateAlternateColorCodes('&', section.getString("name", ""));
        List<String> lore = new ArrayList<>();
        
        if (section.contains("lore")) {
            for (String line : section.getStringList("lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void reload() {
        loadMenus();
    }
}