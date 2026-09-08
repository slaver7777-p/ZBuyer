package dev.slava7777.buyer.db;

import dev.slava7777.buyer.DBuyer;
import dev.slava7777.buyer.buyer.BuyerMultiplier;
import dev.slava7777.buyer.buyer.auto.BuyerStatus;

import java.util.HashSet;
import java.util.Set;

public class BuyerUser {
    private String uuid;
    private double points = 0.0f;
    private double totalPoints;
    private final BuyerMultiplier buyerMultiplier;
    private float multiplier;
    private boolean customMultiplier = false;
    private BuyerStatus buyerStatus = BuyerStatus.DISABLED;
    private final Set<String> autoBuyItemIds = new HashSet<>();

    public BuyerUser(String uuid) {
        this.uuid = uuid;
        this.buyerMultiplier = DBuyer.plugin().config.buyerMultiplier;
        this.multiplier = 1.0f;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(double totalPoints) {
        this.totalPoints = totalPoints;

        if (!customMultiplier) {
            this.multiplier = buyerMultiplier.calculate(totalPoints);
        }
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public void addPoints(double amount) {
        this.points += amount;
        this.totalPoints += amount;
        this.multiplier = buyerMultiplier.calculate(totalPoints);
    }
    public float getMultiplier() {
        if (customMultiplier) {
            return multiplier;
        }
        return buyerMultiplier.calculate(totalPoints);
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
        this.customMultiplier = true;
    }

    public void resetMultiplierToAuto() {
        this.customMultiplier = false;
        this.multiplier = 1.0f;
    }

    public BuyerStatus getBuyerStatus() {
        return buyerStatus;
    }

    public void setBuyerStatus(BuyerStatus mode) {
        this.buyerStatus = mode;
    }

    public boolean isAutoBuyEnabled() {
        return buyerStatus.isActive();
    }

    public Set<String> getAutoBuyItemIds() {
        return autoBuyItemIds;
    }

    public boolean isCustomMultiplier() {
        return customMultiplier;
    }

    public void setCustomMultiplier(boolean customMultiplier) {
        this.customMultiplier = customMultiplier;
    }

    public void toggleAutoBuyItem(String itemId) {
        if (autoBuyItemIds.contains(itemId)) {
            autoBuyItemIds.remove(itemId);
        } else {
            autoBuyItemIds.add(itemId);
        }
    }

    public void enableAutoBuyItem(String itemId) {
        autoBuyItemIds.add(itemId);
    }

    public void disableAutoBuyItem(String itemId) {
        autoBuyItemIds.remove(itemId);
    }

    public boolean hasAutoBuyItem(String itemId) {
        return autoBuyItemIds.contains(itemId);
    }
}
