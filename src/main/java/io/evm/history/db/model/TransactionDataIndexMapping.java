package io.evm.history.db.model;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import io.evm.history.db.model.core.IIndexMapping;

public class TransactionDataIndexMapping implements IIndexMapping {

    public static IIndexMapping INSTANCE = new TransactionDataIndexMapping();

    @Override
    public ObjectBuilder<TypeMapping> mapping(TypeMapping.Builder mapping) {
        //TODO
        return mapping;
    }

    @Override
    public ObjectBuilder<IndexSettings> settings(IndexSettings.Builder settings) {
        //TODO
        return settings;
    }
}
