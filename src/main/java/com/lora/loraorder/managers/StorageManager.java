package com.lora.loraorder.managers;

import com.lora.loraorder.LoraOrder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager {
    
    private final LoraOrder plugin;
    private File storageFile;
    private FileConfiguration storageConfig;
    private final Map<UUID, Map<Material, Integer>> playerStorage;
    
    public StorageManager(LoraOrder plugin) {
        this.plugin = plugin;
        this.playerStorage = new HashMap<>();
        loadStorage();
    }
    
    private void loadStorage() {
        storageFile = new File(plugin.getDataFolder(), "storage.yml");
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
        
        if (storageConfig.contains("storage")) {
            for (String uuidStr : storageConfig.getConfigurationSection("storage").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<Material, Integer> items = new HashMap<>();
                
                String path = "storage." + uuidStr;
                if (storageConfig.contains(path + ".items")) {
                    for (String itemStr : storageConfig.getConfigurationSection(path + ".items").getKeys(false)) {
                        Material material = Material.valueOf(itemStr);
                        int amount = storageConfig.getInt(path + ".items." + itemStr);
                        items.put(material, amount);
                    }
                }
                
                playerStorage.put(uuid, items);
            }
        }
    }
    
    public void saveStorage() {
        storageConfig = new YamlConfiguration();
        
        for (Map.Entry<UUID, Map<Material, Integer>> entry : playerStorage.entrySet()) {
            String path = "storage." + entry.getKey().toString();
            Map<Material, Integer> items = entry.getValue();
            
            for (Map.Entry<Material, Integer> itemEntry : items.entrySet()) {
                storageConfig.set(path + ".items." + itemEntry.getKey().name(), itemEntry.getValue());
            }
        }
        
        try {
            storageConfig.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addToStorage(UUID playerUUID, Material item, int amount) {
        Map<Material, Integer> items = playerStorage.getOrDefault(playerUUID, new HashMap<>());
        int currentAmount = items.getOrDefault(item, 0);
        items.put(item, currentAmount + amount);
        playerStorage.put(playerUUID, items);
        saveStorage();
    }
    
    public boolean removeFromStorage(UUID playerUUID, Material item, int amount) {
        Map<Material, Integer> items = playerStorage.get(playerUUID);
        if (items == null) return false;
        
        int currentAmount = items.getOrDefault(item, 0);
        if (currentAmount < amount) return false;
        
        int newAmount = currentAmount - amount;
        if (newAmount == 0) {
            items.remove(item);
        } else {
            items.put(item, newAmount);
        }
        
        saveStorage();
        return true;
    }
    
    public int getStorageAmount(UUID playerUUID, Material item) {
        Map<Material, Integer> items = playerStorage.get(playerUUID);
        if (items == null) return 0;
        return items.getOrDefault(item, 0);
    }
    
    public Map<Material, Integer> getPlayerStorage(UUID playerUUID) {
        return playerStorage.getOrDefault(playerUUID, new HashMap<>());
    }
    
    public Inventory createStorageInventory(UUID playerUUID) {
        String title = plugin.getMessageManager().getMessage("menu-storage-title");
        Inventory inv = Bukkit.createInventory(null, 54, title);
        Map<Material, Integer> items = getPlayerStorage(playerUUID);
        
        int slot = 0;
        for (Map.Entry<Material, Integer> entry : items.entrySet()) {
            if (slot >= 54) break;
            
            ItemStack item = new ItemStack(entry.getKey(), Math.min(entry.getValue(), 64));
            inv.setItem(slot, item);
            slot++;
        }
        
        return inv;
    }
}