package com.lora.loraorder.managers;

import com.lora.loraorder.LoraOrder;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final LoraOrder plugin;
    private FileConfiguration config;
    
    public ConfigManager(LoraOrder plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public int getMaxActiveOrders() {
        return config.getInt("max-active-orders", 5);
    }
    
    public int getMaxOrderAmount() {
        return config.getInt("max-order-amount", 9999);
    }
    
    public double getMinOrderPrice() {
        return config.getDouble("min-order-price", 1.0);
    }
    
    public double getMaxOrderPrice() {
        return config.getDouble("max-order-price", 1000000.0);
    }
    
    public boolean isAnnouncementsEnabled() {
        return config.getBoolean("announcements.enabled", true);
    }
}
