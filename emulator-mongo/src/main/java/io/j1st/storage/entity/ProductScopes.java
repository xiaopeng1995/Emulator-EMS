package io.j1st.storage.entity;

/**
 * Product Scopes Description Product Type
 */
public enum ProductScopes {

    PUBLIC(1),
    PRIVATE(2);

    private final int value;

    ProductScopes(int value) {
        this.value = value;
    }

    public static ProductScopes valueOf(int value) {
        for (ProductScopes t : values()) {
            if (t.value == value) {
                return t;
            }
        }
        throw new IllegalArgumentException("invalid product status: " + value);
    }

    public int value() {
        return value;
    }
}
