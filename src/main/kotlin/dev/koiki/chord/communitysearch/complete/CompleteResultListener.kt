package dev.koiki.chord.communitysearch.complete

import brave.propagation.CurrentTraceContext
import brave.propagation.TraceContext
import dev.koiki.chord.communitysearch.CommunityComplete
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.suggest.completion.CompletionSuggestion
import org.slf4j.LoggerFactory
import reactor.core.publisher.MonoSink
import kotlin.streams.toList

/**
 * The magic of `scope` val is to use same trace.
 * Don't get TraceContext instance in this class by `currentTraceContext.get()`, it should be instance in a class which invokes this.
 * See details in CurrentTraceContext.wrap(...) method.
 */
class CompleteResultListener(
        private val sink: MonoSink<List<CommunityComplete>>,
        private val currentTraceContext: CurrentTraceContext,
        private val traceContext: TraceContext
) : ActionListener<SearchResponse> {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun onResponse(response: SearchResponse) {
        val scope = currentTraceContext.maybeScope(traceContext)

        scope.use {
            if (log.isDebugEnabled)
                log.debug("complete suggestion result: $response")

            val options = response.suggest
                    .getSuggestion<CompletionSuggestion>("my_suggestion")
                    .options

            val completes = options.stream()
                    .map { CommunityComplete(it.text.string(), it.score) }
                    .toList()

            sink.success(completes)
        }
    }

    override fun onFailure(e: Exception) {
        val scope = currentTraceContext.maybeScope(traceContext)

        scope.use {
            sink.error(e)
        }
    }
}