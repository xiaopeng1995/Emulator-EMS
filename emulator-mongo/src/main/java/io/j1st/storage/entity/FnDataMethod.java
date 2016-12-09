package io.j1st.storage.entity;

/**
 * Fn Button Params Object enums
 */
public enum FnDataMethod {

    FN_DATA_METHOD_INPUT(1),
    FN_DATA_METHOD_FIXED(2),
    FN_DATA_METHOD_UPSTREAM(3),
    FN_DATA_METHOD_CUSTOMER(4);

    private final int value;

    FnDataMethod(int value) {
        this.value = value;
    }

    public static FnDataMethod valueOf(int value) {
        for (FnDataMethod t : values()) {
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
