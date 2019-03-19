package dev.koiki.chord.communitysearch.complete

import brave.Tracing
import dev.koiki.chord.communitysearch.CommunityComplete
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.suggest.SuggestBuilder
import org.elasticsearch.search.suggest.SuggestBuilders
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CommunityCompleteService(
        val client: RestHighLevelClient,
        val tracing: Tracing
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun complete(keyword: String): Mono<List<CommunityComplete>> {
        val termSuggestionBuilder = SuggestBuilders.completionSuggestion("searchCompletion").text(keyword)
        val suggestBuilder = SuggestBuilder().addSuggestion("my_suggestion", termSuggestionBuilder)

        val searchSourceBuilder = SearchSourceBuilder().suggest(suggestBuilder)

        if (log.isDebugEnabled)
            log.debug("complete query: $searchSourceBuilder")

        val request = SearchRequest()
                .indices("chord")
                .source(searchSourceBuilder)

        val currentTraceContext = this.tracing.currentTraceContext()
        val traceContext = this.tracing.currentTraceContext().get()

        return Mono.create { sink ->
            client.searchAsync(request, RequestOptions.DEFAULT, CompleteResultListener(sink, currentTraceContext, traceContext))
        }
    }
}