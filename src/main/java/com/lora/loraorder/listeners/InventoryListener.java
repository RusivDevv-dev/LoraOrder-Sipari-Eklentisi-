package com.lora.loraorder.listeners;

import com.lora.loraorder.LoraOrder;
import com.lora.loraorder.models.Order;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryListener implements Listener {
    
    private final LoraOrder plugin;
    private final Map<Player, Material> selectedItems = new HashMap<>();
    private final Map<Player, Integer> orderAmounts = new HashMap<>();
    private final Map<Player, Double> orderPrices = new HashMap<>();
    
    public InventoryListener(LoraOrder plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        
        // Sadece LoraOrder menülerinde işlem yap
        boolean isLoraOrderMenu = false;
        
        // Ana menü kontrolü
        if (title.contains("Sipariş Sistemi")) {
            isLoraOrderMenu = true;
            e.setCancelled(true);
            handleMainMenu(player, e.getSlot());
        } 
        // Kategori menüsü
        else if (title.contains("Kategori Seçin")) {
            isLoraOrderMenu = true;
            e.setCancelled(true);
            handleCategoryMenu(player, e.getSlot(), e.getCurrentItem());
        } 
        // Item seçim menüsü
        else if (title.contains("Ürün Seçin")) {
            isLoraOrderMenu = true;
            e.setCancelled(true);
            handleItemSelectionMenu(player, e.getSlot(), e.getCurrentItem());
        } 
        // Sipariş oluşturma menüsü
        else if (title.contains("Sipariş Oluştur")) {
            isLoraOrderMenu = true;
            e.setCancelled(true);
            handleOrderCreationMenu(player, e.getSlot());
        } 
        // Siparişler menüsü
        else if (title.contains("Aktif Siparişler")) {
            isLoraOrderMenu = true;
            e.setCancelled(true);
            handleOrdersMenu(player, e.getSlot(), e.getCurrentItem());
        } 
        // Depo menüsü
        else if (title.contains("Deponuz")) {
            isLoraOrderMenu = true;
            e.setCancelled(true);
            handleStorageMenu(player, e.getSlot(), e.getCurrentItem());
        }
        
        // Eğer LoraOrder menüsü değilse, hiçbir şey yapma - diğer pluginler çalışsın
    }
    
    private void handleMainMenu(Player player, int slot) {
        if (slot == 11) { // Sipariş Ver
            player.openInventory(plugin.getMenuManager().createCategoryMenu());
        } else if (slot == 13) { // Siparişler
            openOrdersMenu(player);
        } else if (slot == 15) { // Depo
            player.openInventory(plugin.getStorageManager().createStorageInventory(player.getUniqueId()));
        }
    }
    
    private void handleCategoryMenu(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        
        // Geri butonu kontrolü
        if (slot == 49) {
            player.openInventory(plugin.getMenuManager().createMainMenu());
            return;
        }
        
        // Kategori ikonlarının display name'inden kategoriyi bul
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            String category = null;
            
            if (displayName.contains("Tarım")) {
                category = "farms";
            } else if (displayName.contains("Maden")) {
                category = "miner";
            } else if (displayName.contains("Blok")) {
                category = "blocks";
            }
            
            if (category != null) {
                openItemSelectionMenu(player, category);
            }
        }
    }
    
    private void openItemSelectionMenu(Player player, String category) {
        List<Material> items = plugin.getCategoryManager().getCategoryItems(category);
        String title = plugin.getMessageManager().getMessage("menu-item-selection-title", 
                "{category}", category.toUpperCase());
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        int slot = 0;
        for (Material material : items) {
            if (slot >= 45) break;
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String itemName = material.name().replace("_", " ");
                meta.setDisplayName(ChatColor.GREEN + itemName);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Tıklayarak seçin");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
        }
        
        // Geri butonu
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Geri");
            back.setItemMeta(backMeta);
        }
        inv.setItem(49, back);
        
        player.openInventory(inv);
    }
    
    private void handleItemSelectionMenu(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        
        if (slot == 49) { // Geri butonu
            player.openInventory(plugin.getMenuManager().createCategoryMenu());
            return;
        }
        
        // 0-44 arası slotlarda itemler var
        if (slot >= 0 && slot < 45) {
            // Seçilen item'i kaydet
            selectedItems.put(player, item.getType());
            
            // Sipariş oluşturma menüsünü aç
            Inventory createMenu = plugin.getMenuManager().createOrderCreationMenu();
            
            // Seçilen item'i önizleme slotuna ekle (slot 15)
            ItemStack preview = new ItemStack(item.getType());
            ItemMeta meta = preview.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Seçilen: " + ChatColor.WHITE + item.getType().name().replace("_", " "));
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Bu ürün için sipariş açıyorsunuz");
                meta.setLore(lore);
                preview.setItemMeta(meta);
            }
            createMenu.setItem(4, preview); // Üstte ortada göster
            
            player.openInventory(createMenu);
        }
    }
    
    private void handleOrderCreationMenu(Player player, int slot) {
        if (slot == 11) { // Fiyat Gir
            player.closeInventory();
            startPriceConversation(player);
        } else if (slot == 13) { // Miktar Gir
            player.closeInventory();
            startAmountConversation(player);
        } else if (slot == 15) { // Sipariş Önizlemesi
            showOrderPreview(player);
        } else if (slot == 22) { // Sipariş Oluştur
            createOrder(player);
        } else if (slot == 18) { // Geri
            player.closeInventory();
            selectedItems.remove(player);
            orderAmounts.remove(player);
            orderPrices.remove(player);
            // Item seçim menüsüne geri dönemeyiz, kategori menüsüne dönelim
            player.openInventory(plugin.getMenuManager().createCategoryMenu());
        }
    }
    
    private void startPriceConversation(Player player) {
        ConversationFactory factory = new ConversationFactory(plugin)
                .withFirstPrompt(new PricePrompt())
                .withLocalEcho(false)
                .withTimeout(60)
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        plugin.getMessageManager().sendMessage((Player) event.getContext().getForWhom(), "prompt-cancelled");
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> 
                        player.openInventory(plugin.getMenuManager().createOrderCreationMenu())
                    );
                });
        
        factory.buildConversation(player).begin();
    }
    
    private void startAmountConversation(Player player) {
        ConversationFactory factory = new ConversationFactory(plugin)
                .withFirstPrompt(new AmountPrompt())
                .withLocalEcho(false)
                .withTimeout(60)
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        player.sendMessage(ChatColor.RED + "İşlem iptal edildi.");
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> 
                        player.openInventory(plugin.getMenuManager().createOrderCreationMenu())
                    );
                });
        
        factory.buildConversation(player).begin();
    }
    
    private class PricePrompt extends NumericPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return plugin.getMessageManager().getMessage("prompt-price-input");
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            Player player = (Player) context.getForWhom();
            double price = input.doubleValue();
            
            if (price < plugin.getConfigManager().getMinOrderPrice()) {
                plugin.getMessageManager().sendMessage(player, "prompt-price-too-low",
                        "{min}", String.valueOf(plugin.getConfigManager().getMinOrderPrice()));
                return this;
            }
            
            if (price > plugin.getConfigManager().getMaxOrderPrice()) {
                plugin.getMessageManager().sendMessage(player, "prompt-price-too-high",
                        "{max}", String.valueOf(plugin.getConfigManager().getMaxOrderPrice()));
                return this;
            }
            
            orderPrices.put(player, price);
            plugin.getMessageManager().sendMessage(player, "prompt-price-set",
                    "{price}", String.format("%.2f", price));
            return END_OF_CONVERSATION;
        }
    }
    
    private class AmountPrompt extends NumericPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            return plugin.getMessageManager().getMessage("prompt-amount-input");
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            Player player = (Player) context.getForWhom();
            int amount = input.intValue();
            
            if (amount <= 0) {
                plugin.getMessageManager().sendMessage(player, "prompt-amount-invalid");
                return this;
            }
            
            if (amount > plugin.getConfigManager().getMaxOrderAmount()) {
                plugin.getMessageManager().sendMessage(player, "prompt-amount-too-high",
                        "{max}", String.valueOf(plugin.getConfigManager().getMaxOrderAmount()));
                return this;
            }
            
            orderAmounts.put(player, amount);
            plugin.getMessageManager().sendMessage(player, "prompt-amount-set",
                    "{amount}", String.valueOf(amount));
            return END_OF_CONVERSATION;
        }
    }
    
    private void showOrderPreview(Player player) {
        Material item = selectedItems.get(player);
        Integer amount = orderAmounts.get(player);
        Double price = orderPrices.get(player);
        
        if (item == null || amount == null || price == null) {
            plugin.getMessageManager().sendMessage(player, "incomplete-order-data");
            return;
        }
        
        String itemName = item.name().replace("_", " ");
        double total = amount * price;
        double currentBalance = plugin.getEconomy().getBalance(player);
        
        player.sendMessage(plugin.getMessageManager().getMessage("order-preview-title"));
        player.sendMessage(plugin.getMessageManager().getMessage("order-preview-header"));
        player.sendMessage(plugin.getMessageManager().getMessage("order-preview-item", "{item}", itemName));
        player.sendMessage(plugin.getMessageManager().getMessage("order-preview-amount", "{amount}", String.valueOf(amount)));
        player.sendMessage(plugin.getMessageManager().getMessage("order-preview-price", "{price}", String.format("%.2f", price)));
        player.sendMessage(plugin.getMessageManager().getMessage("order-preview-total", "{total}", String.format("%.2f", total)));
        player.sendMessage(plugin.getMessageManager().getMessage("order-preview-balance", 
                "{balance}", String.format("%.2f", currentBalance)));
        
        // Yeterli parası var mı göster
        if (currentBalance >= total) {
            player.sendMessage(plugin.getMessageManager().getMessage("order-preview-sufficient"));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage("order-preview-insufficient"));
        }
        
        player.sendMessage(plugin.getMessageManager().getMessage("order-preview-footer"));
    }
    
    private void createOrder(Player player) {
        Material item = selectedItems.get(player);
        Integer amount = orderAmounts.get(player);
        Double price = orderPrices.get(player);
        
        if (item == null || amount == null || price == null) {
            plugin.getMessageManager().sendMessage(player, "incomplete-order-data");
            return;
        }
        
        // Aktif sipariş limitini kontrol et
        List<Order> playerOrders = plugin.getOrderManager().getPlayerOrders(player.getUniqueId());
        int activeOrders = 0;
        for (Order order : playerOrders) {
            if (!order.isCompleted()) activeOrders++;
        }
        
        int maxOrders = plugin.getConfigManager().getMaxActiveOrders();
        if (activeOrders >= maxOrders) {
            plugin.getMessageManager().sendMessage(player, "max-orders-reached",
                    "{limit}", String.valueOf(maxOrders));
            return;
        }
        
        // Toplam ödeme hesapla
        double totalPayment = amount * price;
        
        // Oyuncunun yeterli parası var mı kontrol et
        if (plugin.getEconomy().getBalance(player) < totalPayment) {
            plugin.getMessageManager().sendMessage(player, "not-enough-money",
                    "{required}", String.format("%.2f", totalPayment),
                    "{balance}", String.format("%.2f", plugin.getEconomy().getBalance(player)));
            return;
        }
        
        // Parayı çek
        plugin.getEconomy().withdrawPlayer(player, totalPayment);
        
        String orderId = plugin.getOrderManager().createOrder(player, item, amount, price);
        
        plugin.getMessageManager().sendMessage(player, "order-created",
                "{id}", orderId,
                "{item}", item.name().replace("_", " "),
                "{amount}", String.valueOf(amount),
                "{price}", String.format("%.2f", price),
                "{total}", String.format("%.2f", totalPayment));
        
        // Verileri temizle
        selectedItems.remove(player);
        orderAmounts.remove(player);
        orderPrices.remove(player);
        
        player.closeInventory();
    }
    
    private void openOrdersMenu(Player player) {
        List<Order> orders = plugin.getOrderManager().getActiveOrders();
        String title = plugin.getMessageManager().getMessage("menu-orders-title");
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        int slot = 0;
        for (Order order : orders) {
            if (slot >= 45) break;
            
            ItemStack item = new ItemStack(order.getItem());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + order.getItem().name().replace("_", " "));
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "ID: " + ChatColor.WHITE + order.getId());
                lore.add(ChatColor.GRAY + "Oluşturan: " + ChatColor.WHITE + order.getCreatorName());
                lore.add(ChatColor.GRAY + "Kalan: " + ChatColor.WHITE + order.getRemainingAmount() + "/" + order.getTotalAmount());
                lore.add(ChatColor.GRAY + "Fiyat: " + ChatColor.GREEN + order.getPricePerItem() + " TL/adet");
                lore.add(ChatColor.GRAY + "Toplam: " + ChatColor.GREEN + String.format("%.2f", order.getRemainingAmount() * order.getPricePerItem()) + " TL");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Tıklayarak sipariş verin!");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
        }
        
        // Geri butonu
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Geri");
            back.setItemMeta(backMeta);
        }
        inv.setItem(49, back);
        
        player.openInventory(inv);
    }
    
    private void handleOrdersMenu(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        
        if (slot == 49) { // Geri butonu
            player.openInventory(plugin.getMenuManager().createMainMenu());
            return;
        }
        
        if (slot >= 45) return;
        
        // Sipariş ID'sini al
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null) return;
        
        String idLine = meta.getLore().get(0);
        String orderId = ChatColor.stripColor(idLine).replace("ID: ", "");
        
        Order order = plugin.getOrderManager().getOrder(orderId);
        if (order == null) {
            plugin.getMessageManager().sendMessage(player, "order-not-found");
            return;
        }
        
        // Kendi siparişine teslim edemez
        if (order.getCreatorUUID().equals(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "cannot-fulfill-own-order");
            return;
        }
        
        player.closeInventory();
        startFulfillConversation(player, order);
    }
    
    private void startFulfillConversation(Player player, Order order) {
        ConversationFactory factory = new ConversationFactory(plugin)
                .withFirstPrompt(new FulfillPrompt(order))
                .withLocalEcho(false)
                .withTimeout(60)
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        player.sendMessage(ChatColor.RED + "İşlem iptal edildi.");
                    }
                });
        
        factory.buildConversation(player).begin();
    }
    
    private class FulfillPrompt extends NumericPrompt {
        private final Order order;
        
        public FulfillPrompt(Order order) {
            this.order = order;
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.GOLD + "Kaç adet teslim etmek istiyorsunuz? (Max: " + order.getRemainingAmount() + ", iptal için 'cancel'):";
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            Player player = (Player) context.getForWhom();
            int amount = input.intValue();
            
            if (amount <= 0 || amount > order.getRemainingAmount()) {
                player.sendMessage(ChatColor.RED + "Geçersiz miktar!");
                return END_OF_CONVERSATION;
            }
            
            // Oyuncunun envanterinde yeterli item var mı kontrol et
            int playerAmount = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == order.getItem()) {
                    playerAmount += item.getAmount();
                }
            }
            
            if (playerAmount < amount) {
                plugin.getMessageManager().sendMessage(player, "not-enough-items");
                return END_OF_CONVERSATION;
            }
            
            // Item'ları envanterden al
            int remaining = amount;
            for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() == order.getItem()) {
                    int toRemove = Math.min(item.getAmount(), remaining);
                    item.setAmount(item.getAmount() - toRemove);
                    remaining -= toRemove;
                }
            }
            
            // Parayı ver
            double payment = amount * order.getPricePerItem();
            plugin.getEconomy().depositPlayer(player, payment);
            
            // Sipariş sahibinin deposuna ekle
            plugin.getStorageManager().addToStorage(order.getCreatorUUID(), order.getItem(), amount);
            
            // Siparişi güncelle
            plugin.getOrderManager().fulfillOrder(order.getId(), amount);
            
            plugin.getMessageManager().sendMessage(player, "order-fulfilled",
                    "{amount}", String.valueOf(amount),
                    "{item}", order.getItem().name().replace("_", " "),
                    "{payment}", String.format("%.2f", payment));
            
            return END_OF_CONVERSATION;
        }
    }
    
    private void handleStorageMenu(Player player, int slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        
        Material material = item.getType();
        int storageAmount = plugin.getStorageManager().getStorageAmount(player.getUniqueId(), material);
        
        if (storageAmount <= 0) return;
        
        player.closeInventory();
        startWithdrawConversation(player, material, storageAmount);
    }
    
    private void startWithdrawConversation(Player player, Material material, int maxAmount) {
        ConversationFactory factory = new ConversationFactory(plugin)
                .withFirstPrompt(new WithdrawPrompt(material, maxAmount))
                .withLocalEcho(false)
                .withTimeout(60)
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        player.sendMessage(ChatColor.RED + "İşlem iptal edildi.");
                    }
                });
        
        factory.buildConversation(player).begin();
    }
    
    private class WithdrawPrompt extends NumericPrompt {
        private final Material material;
        private final int maxAmount;
        
        public WithdrawPrompt(Material material, int maxAmount) {
            this.material = material;
            this.maxAmount = maxAmount;
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.GOLD + "Kaç adet çekmek istiyorsunuz? (Max: " + maxAmount + ", iptal için 'cancel'):";
        }
        
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            Player player = (Player) context.getForWhom();
            int amount = input.intValue();
            
            if (amount <= 0 || amount > maxAmount) {
                player.sendMessage(ChatColor.RED + "Geçersiz miktar!");
                return END_OF_CONVERSATION;
            }
            
            // Envanterinde yer var mı kontrol et
            if (player.getInventory().firstEmpty() == -1) {
                plugin.getMessageManager().sendMessage(player, "inventory-full");
                return END_OF_CONVERSATION;
            }
            
            // Depodan çıkar ve oyuncuya ver
            if (plugin.getStorageManager().removeFromStorage(player.getUniqueId(), material, amount)) {
                player.getInventory().addItem(new ItemStack(material, amount));
                plugin.getMessageManager().sendMessage(player, "items-withdrawn",
                        "{amount}", String.valueOf(amount),
                        "{item}", material.name().replace("_", " "));
            } else {
                plugin.getMessageManager().sendMessage(player, "withdraw-failed");
            }
            
            return END_OF_CONVERSATION;
        }
    }
}
