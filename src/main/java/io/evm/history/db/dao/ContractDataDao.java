package io.evm.history.db.dao;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.ResponseBody;
import io.evm.history.config.ContractIndexConfig;
import io.evm.history.db.dao.core.CrudDao;
import io.evm.history.db.model.ContractData;
import io.evm.history.db.model.mapping.ContractDataIndexMapping;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("unused")
@ApplicationScoped
public class ContractDataDao extends CrudDao<ContractData> {

    public static String SCROLL_TIME = "60s";

    // for dependency injection
    public ContractDataDao() {
        super(null, null, null, null);
    }

    @Inject
    public ContractDataDao(ContractIndexConfig config, ElasticsearchAsyncClient client) {
        super(config, client, ContractDataIndexMapping.INSTANCE, ContractData.class);
    }

    public Uni<ConcurrentMap<String, Integer>> getAllContracts() {
        return getAllContracts(new ConcurrentHashMap<>(), null);
    }

    protected Uni<ConcurrentMap<String, Integer>> getAllContracts(ConcurrentMap<String, Integer> map, String scrollId) {
        Uni<? extends ResponseBody<ContractData>> res;
        if (scrollId == null) {
            res = search(r -> r.size(10000).scroll(s -> s.time(SCROLL_TIME)), true);
        } else {
            res = scroll(r -> r.scrollId(scrollId).scroll(s -> s.time(SCROLL_TIME)));
        }
        return res.chain(resp -> {
            Optional<List<Hit<ContractData>>> hits = Optional.ofNullable(resp)
                    .map(ResponseBody::hits)
                    .map(HitsMetadata::hits);
            if (hits.isPresent() && !hits.get().isEmpty()) {
                hits.get()
                        .stream()
                        .filter(e -> e.id() != null && e.source() != null)
                        .forEach(e -> map.put(e.id(), e.source().getCodeBytesLength()));
                return getAllContracts(map, resp.scrollId());
            } else {
                return Uni.createFrom().item(map);
            }
        });
    }
}
