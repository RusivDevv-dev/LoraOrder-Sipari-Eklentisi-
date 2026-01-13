package com.lora.loraorder.commands;

import com.lora.loraorder.LoraOrder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OrderCommand implements CommandExecutor {
    
    private final LoraOrder plugin;
    
    public OrderCommand(LoraOrder plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("loraorder.use")) {
            plugin.getMessageManager().sendMessage(player, "no-permission");
            return true;
        }
        
        // Ana menüyü aç
        player.openInventory(plugin.getMenuManager().createMainMenu());
        
        return true;
    }
}