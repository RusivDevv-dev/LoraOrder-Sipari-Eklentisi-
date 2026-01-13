package com.lora.loraorder.managers;

import com.lora.loraorder.LoraOrder;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class MessageManager {
    
    private final LoraOrder plugin;
    private FileConfiguration messages;
    private File messagesFile;
    
    public MessageManager(LoraOrder plugin) {
        this.plugin = plugin;
        createMessagesFile();
    }
    
    private void createMessagesFile() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public void reload() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public String getMessage(String path) {
        String msg = messages.getString(path, "&cMesaj bulunamadı: " + path);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    
    public String getMessage(String path, String... replacements) {
        String msg = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace(replacements[i], replacements[i + 1]);
            }
        }
        return msg;
    }
    
    public void sendMessage(Player player, String path) {
        player.sendMessage(getMessage(path));
    }
    
    public void sendMessage(Player player, String path, String... replacements) {
        player.sendMessage(getMessage(path, replacements));
    }
}