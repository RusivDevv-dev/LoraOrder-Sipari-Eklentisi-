package com.lora.loraorder.commands;

import com.lora.loraorder.LoraOrder;
import com.lora.loraorder.models.Order;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveOrderCommand implements CommandExecutor {
    
    private final LoraOrder plugin;
    
    public RemoveOrderCommand(LoraOrder plugin) {
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
        
        if (args.length == 0) {
            plugin.getMessageManager().sendMessage(player, "remove-order-usage");
            return true;
        }
        
        String orderId = args[0];
        Order order = plugin.getOrderManager().getOrder(orderId);
        
        if (order == null) {
            plugin.getMessageManager().sendMessage(player, "order-not-found");
            return true;
        }
        
        if (!order.getCreatorUUID().equals(player.getUniqueId()) && !player.hasPermission("loraorder.admin")) {
            plugin.getMessageManager().sendMessage(player, "not-your-order");
            return true;
        }
        
        if (plugin.getOrderManager().removeOrder(orderId, player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "order-removed", 
                    "{id}", orderId);
        } else {
            plugin.getMessageManager().sendMessage(player, "order-remove-failed");
        }
        
        return true;
    }
}