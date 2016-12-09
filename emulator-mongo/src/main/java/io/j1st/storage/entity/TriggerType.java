package io.j1st.storage.entity;

/**
 * Trigger Type
 */
public enum TriggerType {

    CONNECT(1),
    UPSTREAM(2),
    DOWNSTREAM(3),
    DISCONNECT(4);

    private final int value;

    TriggerType(int value) {
        this.value = value;
    }

    public static TriggerType valueOf(int value) {
        for (TriggerType t : values()) {
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
