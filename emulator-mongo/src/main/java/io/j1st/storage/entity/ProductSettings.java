package io.j1st.storage.entity;

import org.joda.time.DateTimeZone;

import java.util.List;

/**
 * Product Settings
 */
public class ProductSettings {

    private DateTimeZone timezone;
    private List<FieldSetting> fields;
    private List<CallbackSetting> callback;

    public DateTimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(DateTimeZone timezone) {
        this.timezone = timezone;
    }

    public List<FieldSetting> getFields() {
        return fields;
    }

    public void setFields(List<FieldSetting> fields) {
        this.fields = fields;
    }

    public List<CallbackSetting> getCallback() {
        return callback;
    }

    public void setCallback(List<CallbackSetting> callback) {
        this.callback = callback;
    }
}
