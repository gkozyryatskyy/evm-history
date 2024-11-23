package io.evm.history.db.dao.core;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;

@AllArgsConstructor
public class EsDao {

    protected final Logger log = Logger.getLogger(this.getClass());
    protected final ElasticsearchAsyncClient client;

}
