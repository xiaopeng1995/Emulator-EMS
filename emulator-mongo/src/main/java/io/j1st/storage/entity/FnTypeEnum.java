package io.j1st.storage.entity;

/**
 * Fn Button Params Object enums
 */
public enum FnTypeEnum {

    FN_BUTTON(1),
    FN_BLOCK(2),
    FN_LINE_CHART(3),
    FN_MESSAGE(4),
    FN_METER(5);

    private final int value;

    FnTypeEnum(int value) {
        this.value = value;
    }

    public static FnTypeEnum valueOf(int value) {
        for (FnTypeEnum t : values()) {
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
