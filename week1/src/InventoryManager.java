import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class FlashSaleInventoryManager {

    // productId -> stock count
    private ConcurrentHashMap<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    // productId -> waiting list (FIFO)
    private ConcurrentHashMap<String, Queue<Long>> waitingList = new ConcurrentHashMap<>();

    // Add product
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new LinkedList<>());
    }

    // Check stock
    public String checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        return stock.get() + " units available";
    }

    // Purchase item (no synchronized)
    public String purchaseItem(String productId, long userId) {

        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        while (true) {

            int currentStock = stock.get();

            if (currentStock > 0) {

                // Atomic update
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "User " + userId + " purchase successful. Remaining stock: " + (currentStock - 1);
                }

            } else {

                Queue<Long> queue = waitingList.get(productId);
                queue.add(userId);

                return "Stock sold out. User " + userId + " added to waiting list at position #" + queue.size();
            }
        }
    }
}

public class InventoryManager {

    public static void main(String[] args) {

        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        // Add product with stock
        manager.addProduct("IPHONE15_256GB", 5);

        // Check stock
        System.out.println(manager.checkStock("IPHONE15_256GB"));

        // Simulate purchase requests
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 101));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 102));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 103));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 104));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 105));

        // Stock finished
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 106));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 107));

        // Final stock check
        System.out.println(manager.checkStock("IPHONE15_256GB"));
    }
}