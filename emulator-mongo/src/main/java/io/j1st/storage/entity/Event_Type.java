package io.j1st.storage.entity;

/**
 * Event Type Description
 */
public enum Event_Type {

    ADD(1),
    UPDATE(2),
    DELETE(3),
    SIGN_IN(4),
    SIGN_UP(5),
    UP(6),
    DOWN(7),
    CONNECT(8),
    DISCONNECT(9),
    ERROR(10),
    BIND(11),
    SUBSCRIBE(12),
    EXPAND(13),
    RETRIEVE_PASSWORD(14),
    UNBIND(15);

    private final int value;

    Event_Type(int value) {
        this.value = value;
    }

    public static Event_Type valueOf(int value) {
        for (Event_Type d : values()) {
            if (d.value == value) {
                return d;
            }
        }
        throw new IllegalArgumentException("invalid event type" + value);
    }

    public int value() {
        return value;
    }
}
