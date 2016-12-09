package io.j1st.storage.entity;

/**
 * Device Type
 */
public enum DeviceType {

    AGENT(1),
    INVERTER(2),
    METER(3),
    PANEL(4),
    SENSOR(5),
    COMBINER(6),
    TRACKER(7),
    STORAGE(8),
    CHARGE(9);

    private final int value;

    DeviceType(int value) {
        this.value = value;
    }

    public static DeviceType valueOf(int value) {
        for (DeviceType t : values()) {
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
