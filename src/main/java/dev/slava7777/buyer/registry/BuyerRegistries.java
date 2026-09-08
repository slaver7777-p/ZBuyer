package dev.slava7777.buyer.registry;

import dev.slava7777.buyer.buyer.BuyerItem;
import dev.slava7777.buyer.buyer.auto.BuyerStatus;
import dev.slava7777.buyer.buyer.category.Category;
import dev.slava7777.buyer.buyer.sort.Sorting;

public class BuyerRegistries {

    public final BuyerRegistry<Sorting> sorting = new BuyerRegistry<>();
    public final BuyerRegistry<Category> categories = new BuyerRegistry<>();
    public final BuyerRegistry<BuyerItem> buyerItems = new BuyerRegistry<>();
    public final BuyerRegistry<BuyerStatus> buyerStatuses = new BuyerRegistry<>();

    public void writeLock() {
        sorting.writeLock();
        categories.writeLock();
        buyerItems.writeLock();
        buyerStatuses.writeLock();
    }
}
