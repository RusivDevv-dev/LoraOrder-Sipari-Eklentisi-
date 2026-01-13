package com.lora.loraorder.managers;

import com.lora.loraorder.LoraOrder;
import com.lora.loraorder.models.Order;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OrderManager {
    
    private final LoraOrder plugin;
    private final Map<String, Order> orders;
    private File ordersFile;
    private FileConfiguration ordersConfig;
    
    public OrderManager(LoraOrder plugin) {
        this.plugin = plugin;
        this.orders = new HashMap<>();
        loadOrders();
    }
    
    private void loadOrders() {
        ordersFile = new File(plugin.getDataFolder(), "orders.yml");
        if (!ordersFile.exists()) {
            try {
                ordersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ordersConfig = YamlConfiguration.loadConfiguration(ordersFile);
        
        if (ordersConfig.contains("orders")) {
            for (String key : ordersConfig.getConfigurationSection("orders").getKeys(false)) {
                String path = "orders." + key;
                UUID creatorUUID = UUID.fromString(ordersConfig.getString(path + ".creator-uuid"));
                String creatorName = ordersConfig.getString(path + ".creator-name");
                Material item = Material.valueOf(ordersConfig.getString(path + ".item"));
                int totalAmount = ordersConfig.getInt(path + ".total-amount");
                int remainingAmount = ordersConfig.getInt(path + ".remaining-amount");
                double pricePerItem = ordersConfig.getDouble(path + ".price-per-item");
                long createdTime = ordersConfig.getLong(path + ".created-time");
                
                Order order = new Order(key, creatorUUID, creatorName, item, 
                                       totalAmount, remainingAmount, pricePerItem, createdTime);
                orders.put(key, order);
            }
        }
    }
    
    public void saveOrders() {
        ordersConfig = new YamlConfiguration();
        
        for (Order order : orders.values()) {
            String path = "orders." + order.getId();
            ordersConfig.set(path + ".creator-uuid", order.getCreatorUUID().toString());
            ordersConfig.set(path + ".creator-name", order.getCreatorName());
            ordersConfig.set(path + ".item", order.getItem().name());
            ordersConfig.set(path + ".total-amount", order.getTotalAmount());
            ordersConfig.set(path + ".remaining-amount", order.getRemainingAmount());
            ordersConfig.set(path + ".price-per-item", order.getPricePerItem());
            ordersConfig.set(path + ".created-time", order.getCreatedTime());
        }
        
        try {
            ordersConfig.save(ordersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String createOrder(Player creator, Material item, int amount, double pricePerItem) {
        String orderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8);
        Order order = new Order(orderId, creator.getUniqueId(), creator.getName(), 
                               item, amount, pricePerItem);
        orders.put(orderId, order);
        saveOrders();
        
        // Duyuru yap
        if (plugin.getConfigManager().isAnnouncementsEnabled()) {
            String announcement = plugin.getMessageManager().getMessage("order-created-announcement",
                    "{item}", getItemName(item),
                    "{amount}", String.valueOf(amount),
                    "{price}", String.format("%.2f", pricePerItem));
            Bukkit.broadcastMessage(announcement);
        }
        
        return orderId;
    }
    
    public boolean removeOrder(String orderId, UUID playerUUID) {
        Order order = orders.get(orderId);
        if (order == null) return false;
        if (!order.getCreatorUUID().equals(playerUUID)) return false;
        
        orders.remove(orderId);
        saveOrders();
        return true;
    }
    
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }
    
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
    
    public List<Order> getActiveOrders() {
        List<Order> activeOrders = new ArrayList<>();
        for (Order order : orders.values()) {
            if (!order.isCompleted()) {
                activeOrders.add(order);
            }
        }
        return activeOrders;
    }
    
    public List<Order> getPlayerOrders(UUID playerUUID) {
        List<Order> playerOrders = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getCreatorUUID().equals(playerUUID)) {
                playerOrders.add(order);
            }
        }
        return playerOrders;
    }
    
    public boolean fulfillOrder(String orderId, int amount) {
        Order order = orders.get(orderId);
        if (order == null || order.isCompleted()) return false;
        
        int newRemaining = Math.max(0, order.getRemainingAmount() - amount);
        order.setRemainingAmount(newRemaining);
        
        if (order.isCompleted()) {
            orders.remove(orderId);
        }
        
        saveOrders();
        return true;
    }
    
    private String getItemName(Material material) {
        String name = material.name().replace("_", " ").toLowerCase();
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(" ");
        }
        return result.toString().trim();
    }
}