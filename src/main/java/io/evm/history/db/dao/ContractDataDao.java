package io.evm.history.db.dao;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.evm.history.config.ContractIndexConfig;
import io.evm.history.db.dao.core.CrudDao;
import io.evm.history.db.model.ContractData;
import io.evm.history.db.model.mapping.ContractDataIndexMapping;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@ApplicationScoped
public class ContractDataDao extends CrudDao<ContractData> {

    // for dependency injection
    public ContractDataDao() {
        super(null, null, null, null);
    }

    @Inject
    public ContractDataDao(ContractIndexConfig config, ElasticsearchAsyncClient client) {
        super(config, client, ContractDataIndexMapping.INSTANCE, ContractData.class);
    }

    public Uni<ConcurrentMap<String, Integer>> getAllContracts() {
        // TODO switch to scroll or distributed cache, like redis
        return search(r -> r.size(10000))
                .map(resp -> resp == null ? new ConcurrentHashMap<>() : resp.hits()
                        .hits()
                        .stream()
                        .filter(e -> e.source() != null)
                        // contract address to CodeBytesLength
                        .collect(Collectors.toMap(Hit::id, e -> e.source()
                                .getCodeBytesLength(), (e1, e2) -> e1, ConcurrentHashMap::new)));
    }

}
