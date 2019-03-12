package dev.koiki.chord.communitysearch

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
        val searchService: CommunitySearchService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun search(request: ServerRequest): Mono<ServerResponse> =
            request.bodyToMono(CommunitySearchRequest::class.java)
                    .doOnNext(CommunitySearchRequest::isValid)
                    .flatMap(searchService::search)
                    .flatMap(::successResponse)
                    .onErrorResume(::errorResponse)

    private fun successResponse(communities: List<Community>): Mono<ServerResponse> =
            ServerResponse
                    .ok()
                    .body(BodyInserters.fromObject(communities))

    private fun errorResponse(t: Throwable): Mono<ServerResponse> =
            when(t) {
                is ValidationException -> {
                    ServerResponse
                            .status(HttpStatus.BAD_REQUEST)
                            .body(BodyInserters.fromObject(
                                    ErrorResponse(
                                            trace = getTrace(),
                                            details = t.errorDetails
                                    )
                            ))
                }

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
