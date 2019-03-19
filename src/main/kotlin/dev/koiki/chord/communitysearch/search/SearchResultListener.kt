package dev.koiki.chord.communitysearch.search

import brave.propagation.CurrentTraceContext
import brave.propagation.TraceContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.koiki.chord.communitysearch.Community
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit
import org.slf4j.LoggerFactory
import reactor.core.publisher.MonoSink

/**
 * The magic of `scope` val is to use same trace.
 * Don't get TraceContext instance in this class by `currentTraceContext.get()`, it should be instance in a class which invokes this.
 * See details in CurrentTraceContext.wrap(...) method.
 */
class SearchResultListener(
        private val sink: MonoSink<List<Community>>,
        private val currentTraceContext: CurrentTraceContext,
        private val traceContext: TraceContext,
        private val mapper: ObjectMapper
) : ActionListener<SearchResponse> {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun onResponse(response: SearchResponse) {
        val scope = currentTraceContext.maybeScope(traceContext)

        scope.use {
            if (log.isDebugEnabled)
                log.debug("search result: $response")

            //TODO create Community instance instead of mapper
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
            sink.error(e)
        }
    }
}