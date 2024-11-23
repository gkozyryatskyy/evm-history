package io.evm.history.config.core;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public interface EsIndexConfig {

    DateTimeFormatter INDEX_FORMAT = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneOffset.UTC);
    String SEARCH_ALIAS_POSTFIX = "-search";
    String TEMPLATE_POSTFIX = "-template";
    String ALL_PATTERN_POSTFIX = "-*";

    String indexPrefix();

    default String templateName() {
        return templateName(indexPrefix());
    }

    default String templateName(String prefix) {
        return prefix + TEMPLATE_POSTFIX;
    }

    default String matchAllIndex() {
        return matchAllIndex(indexPrefix());
    }

    default String matchAllIndex(String prefix) {
        return prefix + ALL_PATTERN_POSTFIX;
    }

    default String searchIndex(Long ts) {
        return searchIndex(indexPrefix(), ts);
    }

    default String searchIndex(String prefix, Long timestamp) {
        if (timestamp == null) {
            return prefix + SEARCH_ALIAS_POSTFIX;
        } else {
            return timeIndex(prefix, Instant.ofEpochMilli(timestamp));
        }
    }

    default String timeIndex(String prefix, Instant timestamp) {
        return timeIndex(prefix, INDEX_FORMAT.format(timestamp));
    }

    default String timeIndex(String prefix, String time) {
        return prefix + "-" + time;
    }
}