package com.lora.loraorder.models;

import org.bukkit.Material;

import java.util.UUID;

public class Order {
    
    private final String id;
    private final UUID creatorUUID;
    private final String creatorName;
    private final Material item;
    private final int totalAmount;
    private int remainingAmount;
    private final double pricePerItem;
    private final long createdTime;
    
    public Order(String id, UUID creatorUUID, String creatorName, Material item, 
                 int amount, double pricePerItem) {
        this.id = id;
        this.creatorUUID = creatorUUID;
        this.creatorName = creatorName;
        this.item = item;
        this.totalAmount = amount;
        this.remainingAmount = amount;
        this.pricePerItem = pricePerItem;
        this.createdTime = System.currentTimeMillis();
    }
    
    public Order(String id, UUID creatorUUID, String creatorName, Material item, 
                 int totalAmount, int remainingAmount, double pricePerItem, long createdTime) {
        this.id = id;
        this.creatorUUID = creatorUUID;
        this.creatorName = creatorName;
        this.item = item;
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
        this.pricePerItem = pricePerItem;
        this.createdTime = createdTime;
    }
    
    public String getId() {
        return id;
    }
    
    public UUID getCreatorUUID() {
        return creatorUUID;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public Material getItem() {
        return item;
    }
    
    public int getTotalAmount() {
        return totalAmount;
    }
    
    public int getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(int amount) {
        this.remainingAmount = amount;
    }
    
    public double getPricePerItem() {
        return pricePerItem;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public double getTotalPrice() {
        return totalAmount * pricePerItem;
    }
    
    public boolean isCompleted() {
        return remainingAmount <= 0;
    }
}