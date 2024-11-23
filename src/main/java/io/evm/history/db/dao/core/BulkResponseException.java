package io.evm.history.db.dao.core;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class BulkResponseException extends RuntimeException {

    private final List<String> reasons;

    public BulkResponseException(List<String> reasons) {
        super("Bulk errors:%s. First reason:%s".formatted(reasons.size(), reasons.getFirst()));
        this.reasons = reasons;
    }

    public static BulkResponseException of(BulkResponse resp) {
        return new BulkResponseException(resp.items()
                .stream()
                .filter(e -> e.error() != null)
                .map(e -> e.error().reason()).toList());
    }
}
