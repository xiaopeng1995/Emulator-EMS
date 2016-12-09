package io.j1st.storage.entity;

/**
 * Fn Button Params Object enums
 */
public enum ChartValueType {
    CURRENT_VALUE(1),
    AVERAGE_VALUE(2),
    MIN_VALUE(3),
    MAX_VALUE(4);

    private final int value;

    ChartValueType(int value) {
        this.value = value;
    }

    public static ChartValueType valueOf(int value) {
        for (ChartValueType t : values()) {
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
