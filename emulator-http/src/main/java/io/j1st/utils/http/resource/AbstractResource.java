package io.j1st.utils.http.resource;

import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;

/**
 * Abstract Base Resource
 */
public class AbstractResource {
    protected MongoStorage mongo;
    protected DataMongoStorage dataMongoStorage;

    public AbstractResource(MongoStorage mongo,DataMongoStorage dataMongoStorage) {
        this.mongo = mongo;
        this.dataMongoStorage=dataMongoStorage;
    }
}
