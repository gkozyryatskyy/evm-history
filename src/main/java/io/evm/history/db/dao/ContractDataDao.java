package io.evm.history.db.dao;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import io.evm.history.config.ContractIndexConfig;
import io.evm.history.db.dao.core.CrudDao;
import io.evm.history.db.model.ContractData;
import io.evm.history.db.model.mapping.ContractDataIndexMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

}
