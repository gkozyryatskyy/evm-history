package io.evm.history.db.model.mapping;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import io.evm.history.db.model._TransactionData;
import io.evm.history.db.model.core.IIndexMapping;

public class TransactionDataIndexMapping implements IIndexMapping {

    public static IIndexMapping INSTANCE = new TransactionDataIndexMapping();

    @Override
    public ObjectBuilder<TypeMapping> mapping(TypeMapping.Builder mapping) {
        return mapping
                // block
                .properties(_TransactionData.timestamp, prop -> prop.date(e -> e))
                .properties(_TransactionData.blockNumber, prop -> prop.long_(e -> e))
                .properties(_TransactionData.blockHash, prop -> prop.keyword(k ->
                        k.fields("ts", e -> e.text(t -> t.analyzer("keyword_edge_ngram")
                                .searchAnalyzer("keyword_edge_ngram_search")))))
                // tx
                .properties(_TransactionData.txHash, prop -> prop.keyword(k ->
                        k.fields("ts", e -> e.text(t -> t.analyzer("keyword_edge_ngram")
                                .searchAnalyzer("keyword_edge_ngram_search")))))
                .properties(_TransactionData.value, prop -> prop.keyword(e -> e))
                .properties(_TransactionData.gas, prop -> prop.keyword(e -> e))
                .properties(_TransactionData.input, prop -> prop.keyword(e -> e))
                .properties(_TransactionData.type, prop -> prop.keyword(e -> e))
                // receipt
                .properties(_TransactionData.contractAddress, prop -> prop.keyword(k ->
                        k.fields("ts", e -> e.text(t -> t.analyzer("keyword_edge_ngram")
                                .searchAnalyzer("keyword_edge_ngram_search")))))
                .properties(_TransactionData.gasUsed, prop -> prop.long_(e -> e))
                .properties(_TransactionData.from, prop -> prop.keyword(k ->
                        k.fields("ts", e -> e.text(t -> t.analyzer("keyword_edge_ngram")
                                .searchAnalyzer("keyword_edge_ngram_search")))))
                .properties(_TransactionData.to, prop -> prop.keyword(e -> e))
                .properties(_TransactionData.codeBytesLength, prop -> prop.integer(e -> e));
    }

    @Override
    public ObjectBuilder<IndexSettings> settings(IndexSettings.Builder settings) {
        return keywordEdgeNgram(settings);
    }
}
