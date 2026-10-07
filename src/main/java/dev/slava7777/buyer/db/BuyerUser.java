package dev.slava7777.buyer.db;

import dev.slava7777.buyer.DBuyer;
import dev.slava7777.buyer.buyer.BuyerMultiplier;
import dev.slava7777.buyer.buyer.auto.BuyerStatus;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class BuyerUser {
    private String uuid;
    private double points = 0.0f;
    private double totalPoints;
    private final BuyerMultiplier buyerMultiplier;
    private float multiplier;
    private boolean customMultiplier = false;
    private BuyerStatus buyerStatus = BuyerStatus.DISABLED;
    private final IntSet autoBuyItemIds = new IntOpenHashSet(128, 0.75f);

    public BuyerUser(String uuid) {
        this.uuid = uuid;
        this.buyerMultiplier = DBuyer.plugin().config.buyerMultiplier();
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

    public IntSet getAutoBuyItemIds() {
        return autoBuyItemIds;
    }

    public boolean isCustomMultiplier() {
        return customMultiplier;
    }

    public void setCustomMultiplier(boolean customMultiplier) {
        this.customMultiplier = customMultiplier;
    }

    public void toggleAutoBuyItem(int id) {
        if (autoBuyItemIds.contains(id)) {
            autoBuyItemIds.remove(id);
        } else {
            autoBuyItemIds.add(id);
        }
    }

    public void enableAutoBuyItem(int id) {
        autoBuyItemIds.add(id);
    }

    public void disableAutoBuyItem(int id) {
        autoBuyItemIds.remove(id);
    }

    public boolean hasAutoBuyItem(int id) {
        return autoBuyItemIds.contains(id);
    }
}
