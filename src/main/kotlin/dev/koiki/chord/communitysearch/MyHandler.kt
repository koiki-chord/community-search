package dev.koiki.chord.communitysearch

import dev.koiki.chord.communitysearch.complete.CommunityCompleteService
import dev.koiki.chord.communitysearch.search.CommunitySearchRequest
import dev.koiki.chord.communitysearch.search.CommunitySearchService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class MyHandler(
        val requestFactory: RequestFactory,
        val searchService: CommunitySearchService,
        val completeService: CommunityCompleteService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun search(request: ServerRequest): Mono<ServerResponse> {
        val searchRequest: CommunitySearchRequest = requestFactory.createSearchRequest(request)

        return searchService.search(searchRequest)
                .flatMap(::successResponse)
                .onErrorResume(::errorResponse)
    }

    fun complete(request: ServerRequest): Mono<ServerResponse> {
        val keyword = request.queryParam("keyword").orElseThrow { RuntimeException("todo") }

        return completeService.complete(keyword)
                .flatMap(::successResponse)
                .onErrorResume(::errorResponse)
    }

    private fun successResponse(body: Any): Mono<ServerResponse> =
            ServerResponse
                    .ok()
                    .body(BodyInserters.fromObject(body))

    private fun errorResponse(t: Throwable): Mono<ServerResponse> =
            when(t) {
                else -> {
                    log.error(t.message, t)

                    ServerResponse
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(BodyInserters.fromObject(
                                    ErrorResponse(
                                            trace = getTrace(),
                                            details = listOf(
                                                    ErrorDetail(
                                                            message = t.message ?: "error message is null"
                                                    )
                                    )
                            )))
                }
            }

    private fun getTrace() = MDC.get("X-B3-TraceId")
}
