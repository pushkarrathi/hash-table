import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class InventoryManager {

    private ConcurrentHashMap<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Queue<Long>> waitingList = new ConcurrentHashMap<>();


    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new LinkedList<>());
    }

    public String checkStock(String productId) {

        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        return stock.get() + " units available";
    }

    public String purchaseItem(String productId, long userId) {

        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        while (true) {

            int currentStock = stock.get();

            // If stock available
            if (currentStock > 0) {

                // atomic decrement
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }

            } else {

                Queue<Long> queue = waitingList.get(productId);
                queue.add(userId);

                return "Added to waiting list, position #" + queue.size();
            }
        }
    }
}