package dev.slava7777.buyer.buyer.sort;

import dev.slava7777.buyer.buyer.BuyerItem;

import java.util.Comparator;

public record Sorting(String id, Comparator<BuyerItem> comparator) {
}
