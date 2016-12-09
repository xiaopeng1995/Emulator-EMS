package io.j1st.storage.entity;

/**
 * Data Format
 */
public enum DataFormat {
    NUMBER(1),
    STRING(2),
    BOOLEAN(3);

    private final int value;

    DataFormat(int value) {
        this.value = value;
    }

    public static DataFormat valueOf(int value) {
        for (DataFormat f : values()) {
            if (f.value == value) {
                return f;
            }
        }
        throw new IllegalArgumentException("invalid data format " + value);
    }

    public int value() {
        return value;
    }
}
