package dev.slava7777.buyer.buyer.auto;

import dev.slava7777.buyer.registry.BuyerRegistries;

public enum BuyerStatus {
    DISABLED,
    ENABLED,
    TEMP_ENABLED;

    public boolean isActive() {
        return this != DISABLED;
    }

    public BuyerStatus next() {
        if (this == TEMP_ENABLED) {
            return DISABLED;
        }
        return this;
    }

    public static void boot(BuyerRegistries registries) {
        registries.buyerStatuses.register(ENABLED.getId(), ENABLED);
        registries.buyerStatuses.register(DISABLED.getId(), DISABLED);
        registries.buyerStatuses.register(TEMP_ENABLED.getId(), TEMP_ENABLED);
    }

    public String getId() {
        return switch (this) {
            case ENABLED -> "enable";
            case DISABLED -> "disable";
            case TEMP_ENABLED -> "temp-enable";
        };
    }
}
