package dev.koiki.chord.communitysearch

import dev.koiki.chord.communitysearch.ErrorCode.*
import dev.koiki.chord.communitysearch.search.CommunitySearchRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class RequestFactory {
    fun createSearchRequest(request: ServerRequest): CommunitySearchRequest =
            CommunitySearchRequest(
                    text = getText(request),
                    size = getSize(request) ?: 20,
                    page = getPage(request) ?: 0
            )

    private fun getText(request: ServerRequest): String {
        val text: String = request.queryParam("text")
                .orElseThrow { ValidationException(TEXT_SHOULD_NOT_BE_NULL, "text should not be null.") }

        if (text.isBlank())
            throw ValidationException(TEXT_SHOULD_NOT_BE_BLANK, "text should not be blank.")

        return text
    }

    private fun getSize(request: ServerRequest): Int? {
        val size: Int? = request.queryParam("size")
                .map { it.toInt() }
                .orElse(null)

        if (size != null && size > 1_000)
            throw ValidationException(SIZE_SHOULD_NOT_BE_GREATER_THAN_1000,
                    "size should not be greater than 1,000.")

        return size
    }

    private fun getPage(request: ServerRequest): Int? = request.queryParam("page")
            .map { it.toInt() }
            .orElse(null)
}