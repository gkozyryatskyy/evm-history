package io.evm.history.db.dao.core;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateResponse;
import io.evm.history.config.core.EsIndexConfig;
import io.evm.history.db.model.core.IIndexMapping;
import io.evm.history.db.model.core.ITimeSeries;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class CrudDao<T extends ITimeSeries> extends EsDao {

    protected final EsIndexConfig config;
    protected final IIndexMapping indexConfig;
    protected final Class<T> type;

    public CrudDao(EsIndexConfig config, ElasticsearchAsyncClient client, IIndexMapping indexConfig, Class<T> type) {
        super(client);
        this.config = config;
        this.indexConfig = indexConfig;
        this.type = type;
    }

    public String index(Long ts) {
        return config.searchIndex(ts);
    }

    // ------------------- TEMPLATE -------------------
    public Uni<PutIndexTemplateResponse> template() {
        String template = config.templateName();
        String indexPatterns = config.matchAllIndex();
        String searchAlias = config.searchIndex(null);
        return Uni.createFrom()
                .completionStage(Unchecked.supplier(() -> {
                    log.infof("Creating template=%s indexPatterns=%s", template, indexPatterns);
                    return client.indices()
                            .putIndexTemplate(req -> req.name(template)
                                    .indexPatterns(indexPatterns)
                                    .template(t -> t
                                            .aliases(searchAlias, a -> a)
                                            .settings(indexConfig::settings)
                                            .mappings(indexConfig::mapping))
                            );
                }));
    }
    // ------------------- BULK -------------------
    public Uni<BulkResponse> bulk(List<T> list) {
        return bulk(list, null);
    }

    public Uni<BulkResponse> bulk(List<T> list, Refresh refresh) {
        return bulk(bulkReq(bulkOpIndex(list), refresh));
    }

    public Uni<BulkResponse> bulk(BulkRequest req) {
        Context context = Vertx.currentContext();
        Uni<BulkResponse> retval = Uni.createFrom()
                .completionStage(Unchecked.supplier(() -> client.bulk(req)))
                .invoke(Unchecked.consumer(e -> {
                    if (e.errors()) {
                        throw BulkResponseException.of(e);
                    }
                }));
        if (context != null) {  // used in tests
            retval = retval.emitOn(context::runOnContext);
        }
        return retval;
    }

    public BulkRequest bulkReq(List<BulkOperation> list, Refresh refresh) {
        BulkRequest.Builder bulk = new BulkRequest.Builder();
        bulk.operations(list);
        if (refresh != null) {
            bulk.refresh(refresh);
        }
        return bulk.build();
    }

    public List<BulkOperation> bulkOpIndex(List<T> list) {
        return list.stream()
                .map(this::bulkOpIndex)
                .toList();
    }

    public BulkOperation bulkOpIndex(T e) {
        return BulkOperation.of(op -> op.index(index -> {
            index.index(index(e.getTs())).document(e);
            return index;
        }));
    }

    public Uni<BulkResponse> mBulk(List<List<BulkOperation>> lists) {
        return mBulk(lists, null);
    }

    public Uni<BulkResponse> mBulk(List<List<BulkOperation>> lists, Refresh refresh) {
        return bulk(mBulkReq(lists, refresh));
    }

    protected BulkRequest mBulkReq(List<List<BulkOperation>> lists, Refresh refresh) {
        BulkRequest.Builder bulk = new BulkRequest.Builder();
        bulk.operations(lists.stream().flatMap(Collection::stream).toList());
        if (refresh != null) {
            bulk.refresh(refresh);
        }
        return bulk.build();
    }

    // ------------------- SEARCH -------------------
    public Uni<SearchResponse<T>> search(Function<SearchRequest.Builder, SearchRequest.Builder> search) {
        return Uni.createFrom().completionStage(Unchecked.supplier(() ->
                        client.search(req -> search.apply(searchReq(req, config)), type)
                ))
                // if the index not yet exists
                .onFailure(e -> e instanceof ElasticsearchException && "index_not_found_exception".equals(((ElasticsearchException) e)
                        .error()
                        .type()))
                .recoverWithNull();
    }

    private SearchRequest.Builder searchReq(SearchRequest.Builder req, EsIndexConfig config) {
        req.index(List.of(config.searchIndex(null)));
        req.trackTotalHits(h -> h.enabled(false)).trackScores(false);
        return req;
    }
}
