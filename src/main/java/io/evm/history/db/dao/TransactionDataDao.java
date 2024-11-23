package io.evm.history.db.dao;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.evm.history.config.TransactionIndexConfig;
import io.evm.history.db.dao.core.CrudDao;
import io.evm.history.db.model.TransactionData;
import io.evm.history.db.model.TransactionDataIndexMapping;
import io.evm.history.db.model.core._Timeseries;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@ApplicationScoped
public class TransactionDataDao extends CrudDao<TransactionData> {

    // for dependency injection
    public TransactionDataDao() {
        super(null, null, null, null);
    }

    @Inject
    public TransactionDataDao(TransactionIndexConfig config, ElasticsearchAsyncClient client) {
        super(config, client, TransactionDataIndexMapping.INSTANCE, TransactionData.class);
    }

    public Uni<BigInteger> findLastBlockNumber() {
        return search(r -> r
                .size(1)
                .sort(List.of(SortOptions.of(s -> s.field(f -> f.field(_Timeseries.timestamp).order(SortOrder.Desc))))))
                .map(e -> e == null || e.hits().hits().isEmpty() ? null : Optional.of(e.hits().hits().getFirst())
                        .map(Hit::source)
                        .map(TransactionData::getBlockNumber)
                        .orElse(null));
    }

}
