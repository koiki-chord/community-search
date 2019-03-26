package dev.koiki.chord.communitysearch.search

import brave.Tracing
import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CommunitySearchService(
        val client: RestHighLevelClient,
        val mapper: ObjectMapper,
        val tracing: Tracing
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun search(request: CommunitySearchRequest): Mono<CommunitySearchResult> {
        val boolQueryBuilder = QueryBuilders.boolQuery()

        boolQueryBuilder.should().add(QueryBuilders.matchQuery("name", request.text).operator(Operator.AND))
        boolQueryBuilder.should().add(QueryBuilders.matchQuery("desc", request.text).operator(Operator.AND))
        boolQueryBuilder.minimumShouldMatch(1)

        val searchSourceBuilder = SearchSourceBuilder()
                .query(boolQueryBuilder)
                .size(request.limit)
                .from(request.offset)

        if (log.isDebugEnabled)
            log.debug("query: $searchSourceBuilder")

        val searchRequest = SearchRequest()
                .indices("chord")
                .source(searchSourceBuilder)

        val currentTraceContext = this.tracing.currentTraceContext()
        val traceContext = this.tracing.currentTraceContext().get()

        return Mono.create { sink ->
            client.searchAsync(searchRequest, RequestOptions.DEFAULT, SearchResultListener(sink, request, currentTraceContext, traceContext, mapper))
        }
    }
}
