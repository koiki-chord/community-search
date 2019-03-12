package dev.koiki.chord.communitysearch

import brave.Tracing
import brave.propagation.CurrentTraceContext
import brave.propagation.TraceContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink

@Service
class CommunitySearchService(
        val client: RestHighLevelClient,
        val mapper: ObjectMapper,
        val tracing: Tracing
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun search(csRequest: CommunitySearchRequest): Mono<List<Community>> {
        val boolQueryBuilder: BoolQueryBuilder = QueryBuilders.boolQuery()
        if (csRequest.text != null) {
            boolQueryBuilder.should().add(QueryBuilders.matchQuery("name", csRequest.text).operator(Operator.AND))
            boolQueryBuilder.should().add(QueryBuilders.matchQuery("desc", csRequest.text).operator(Operator.AND))
            boolQueryBuilder.minimumShouldMatch(1)
        }

        val searchSourceBuilder = SearchSourceBuilder().query(
                boolQueryBuilder
        )

        if (log.isDebugEnabled)
            log.debug("query: $searchSourceBuilder")

        val request = SearchRequest()
                .indices("chord")
                .source(searchSourceBuilder)

        // this magic is needed to propagate current trace to ActionListener.
        // don't get traceContext in ActionListener.
        val currentTraceContext = this.tracing.currentTraceContext()
        val traceContext = this.tracing.currentTraceContext().get()

        return Mono.create { sink ->
            client.searchAsync(request, RequestOptions.DEFAULT, actionListener(sink, currentTraceContext, traceContext))
        }
    }

    private fun actionListener(sink: MonoSink<List<Community>>, currentTraceContext: CurrentTraceContext, traceContext: TraceContext): ActionListener<SearchResponse> =
            object : ActionListener<SearchResponse> {

                override fun onResponse(response: SearchResponse) {
                    val scope = currentTraceContext.maybeScope(traceContext)

                    scope.use {
                        if (log.isDebugEnabled)
                            log.debug("search result: ${mapper.writeValueAsString(response)}")

                        val communities: List<Community> = mapper.readValue(
                                response
                                        .hits
                                        .hits
                                        .joinToString(
                                                transform = SearchHit::getSourceAsString,
                                                prefix = "[", postfix = "]", separator = ","
                                        )
                        )

                        sink.success(communities)
                    }
                }

                override fun onFailure(e: Exception) {
                    val scope = currentTraceContext.maybeScope(traceContext)

                    scope.use {
                        if (log.isDebugEnabled)
                            log.error(e.message, e)

                        sink.error(e)
                    }
                }
            }
}
