package dev.koiki.chord.communitysearch

data class ErrorResponse(
        val trace: String,
        val code: ErrorCode = ErrorCode.INTERNAL_SERVER_ERROR,
        val message: String
)
