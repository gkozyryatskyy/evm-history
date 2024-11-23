package io.evm.history.db.model.mapping;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import io.evm.history.db.model._ContractData;
import io.evm.history.db.model.core.IIndexMapping;

public class ContractDataIndexMapping implements IIndexMapping {

    public static IIndexMapping INSTANCE = new ContractDataIndexMapping();

    @Override
    public ObjectBuilder<TypeMapping> mapping(TypeMapping.Builder mapping) {
        return mapping
                .properties(_ContractData.timestamp, prop -> prop.date(e -> e))
                .properties(_ContractData.address, prop -> prop.keyword(k ->
                        k.fields("ts", e -> e.text(t -> t.analyzer("keyword_edge_ngram")
                                .searchAnalyzer("keyword_edge_ngram_search")))))
                .properties(_ContractData.codeBytesLength, prop -> prop.integer(e -> e));
    }

    @Override
    public ObjectBuilder<IndexSettings> settings(IndexSettings.Builder settings) {
        return keywordEdgeNgram(settings);
    }
}
