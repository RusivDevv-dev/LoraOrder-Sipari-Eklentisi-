package com.lora.loraorder;

import com.lora.loraorder.commands.OrderCommand;
import com.lora.loraorder.commands.RemoveOrderCommand;
import com.lora.loraorder.listeners.InventoryListener;
import com.lora.loraorder.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class LoraOrder extends JavaPlugin {
    
    private static LoraOrder instance;
    private Economy economy;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private MenuManager menuManager;
    private CategoryManager categoryManager;
    private OrderManager orderManager;
    private StorageManager storageManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Vault ekonomi sistemini kur
        if (!setupEconomy()) {
            getLogger().severe("Vault bulunamadı! Plugin devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Yöneticileri başlat
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        menuManager = new MenuManager(this);
        categoryManager = new CategoryManager(this);
        orderManager = new OrderManager(this);
        storageManager = new StorageManager(this);
        
        // Komutları kaydet
        getCommand("sipariş").setExecutor(new OrderCommand(this));
        getCommand("siparişkaldır").setExecutor(new RemoveOrderCommand(this));
        
        // Event listener'ı kaydet
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        
        getLogger().info("LoraOrder başarıyla yüklendi!");
    }
    
    @Override
    public void onDisable() {
        if (orderManager != null) {
            orderManager.saveOrders();
        }
        getLogger().info("LoraOrder kapatıldı!");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public static LoraOrder getInstance() {
        return instance;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public MenuManager getMenuManager() {
        return menuManager;
    }
    
    public CategoryManager getCategoryManager() {
        return categoryManager;
    }
    
    public OrderManager getOrderManager() {
        return orderManager;
    }
    
    public StorageManager getStorageManager() {
        return storageManager;
    }
}
