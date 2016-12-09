package io.j1st.storage.entity;

/**
 * Fn Button Params Object enums
 */
public enum FnDataType {

    String(1),
    Number(2),
    Boolean(3),
    Enumeration(4);

    private final int value;

    FnDataType(int value) {
        this.value = value;
    }

    public static FnDataType valueOf(int value) {
        for (FnDataType t : values()) {
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
