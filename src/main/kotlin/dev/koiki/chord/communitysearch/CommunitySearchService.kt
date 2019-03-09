package dev.koiki.chord.communitysearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.SearchHit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink

@Service
class CommunitySearchService(
        val client: RestHighLevelClient,
        val mapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun findAll(): Mono<List<Community>> = Mono.create { sink ->
        client.searchAsync(SearchRequest(), RequestOptions.DEFAULT, actionListener(sink))
    }

    private fun actionListener(sink: MonoSink<List<Community>>): ActionListener<SearchResponse> = object : ActionListener<SearchResponse> {
        override fun onResponse(response: SearchResponse) {
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

        override fun onFailure(exception: Exception) {
            sink.error(exception)
        }
    }
}
