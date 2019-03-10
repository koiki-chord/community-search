package dev.koiki.chord.communitysearch

import brave.Tracing
import brave.propagation.CurrentTraceContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.SpanNamer
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink

@Service
class CommunitySearchService(
        val client: RestHighLevelClient,
        val mapper: ObjectMapper,
        val tracing: Tracing,
        val spanNamer: SpanNamer
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun findAll(): Mono<List<Community>> {
        val request = SearchRequest()
                .indices("chord")
                .source(SearchSourceBuilder().query(
                        QueryBuilders.matchAllQuery()
                ))

        return Mono.create { sink ->
            client.searchAsync(request, RequestOptions.DEFAULT, actionListener(sink))
        }
    }

    fun search(csRequest: CommunitySearchRequest): Mono<List<Community>> {
        val boolQueryBuilder: BoolQueryBuilder = QueryBuilders.boolQuery()
        csRequest.names.forEach {
            boolQueryBuilder.must().add(QueryBuilders.matchQuery("name", it))
        }

        val searchSourceBuilder = SearchSourceBuilder().query(
                boolQueryBuilder
        )

        if (log.isDebugEnabled)
            log.debug("query: $searchSourceBuilder")

        val request = SearchRequest()
                .indices("chord")
                .source(searchSourceBuilder)

        return Mono.create { sink ->
            client.searchAsync(request, RequestOptions.DEFAULT, actionListener(sink, tracing.currentTraceContext().maybeScope(tracing.currentTraceContext().get())))
        }
    }

    private fun actionListener(sink: MonoSink<List<Community>>, scope: CurrentTraceContext.Scope? = null): ActionListener<SearchResponse> = object : ActionListener<SearchResponse> {
        // to enable Trace & Span, see details in TraceRunnable.class
        private fun getSpan() = tracing.tracer().startScopedSpanWithParent(
                spanNamer.name(this, "actionListener"),
                tracing.currentTraceContext().get()
        )

        override fun onResponse(response: SearchResponse) {
            val span = getSpan()
            //val scope: CurrentTraceContext.Scope? = tracing.currentTraceContext().maybeScope(tracing.currentTraceContext().get())

            try {
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
            } finally {
                span.finish()
                scope?.close()
            }
        }

        override fun onFailure(e: Exception) {
            val span = getSpan()

            try {
                if (log.isDebugEnabled)
                    log.error(e.message, e)

                sink.error(e)
            } finally {
                span.finish()
            }
        }
    }
}
