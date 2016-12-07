package io.j1st.util.entity;

import io.j1st.util.entity.payload.Query;

import java.util.List;

public class Payload {
    private List<Query> Query;

    public void setQuery(List<Query> Query) {
        this.Query = Query;
    }

    public List<Query> getQuery() {
        return this.Query;
    }

}