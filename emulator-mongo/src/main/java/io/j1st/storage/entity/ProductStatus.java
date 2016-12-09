package io.j1st.storage.entity;

/**
 * Product status description
 */
public enum ProductStatus {
    SERVICE(1),
    SUSPEND(2), ;

    private final int value;

    ProductStatus(int value) {
        this.value = value;
    }

    public static ProductStatus valueOf(int value) {
        for (ProductStatus r : values()) {
            if (r.value == value) {
                return r;
            }
        }
        throw new IllegalArgumentException("invalid product status: " + value);
    }

    public int value() {
        return value;
    }
}
