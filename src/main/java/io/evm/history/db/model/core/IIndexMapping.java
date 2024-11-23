package io.evm.history.db.model.core;

import co.elastic.clients.elasticsearch._types.analysis.TokenChar;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;

import java.util.List;

public interface IIndexMapping {

    ObjectBuilder<TypeMapping> mapping(TypeMapping.Builder mapping);

    ObjectBuilder<IndexSettings> settings(IndexSettings.Builder settings);

    default ObjectBuilder<IndexSettings> keywordEdgeNgram(IndexSettings.Builder settings) {
        return settings.analysis(root -> root
                // single term
                .tokenizer("edge_ngram_1_20", t -> t.definition(d -> d.edgeNgram(e -> e
                        .minGram(1)
                        .maxGram(20)
                        .tokenChars(List.of(TokenChar.Letter, TokenChar.Digit)))))
                .analyzer("keyword_edge_ngram", a -> a.custom(c -> c.tokenizer("edge_ngram_1_20")
                        .filter(List.of("lowercase"))))
                .analyzer("keyword_edge_ngram_search", a -> a.custom(c -> c.tokenizer("keyword")
                        .filter(List.of("lowercase"))))
        );
    }
}
