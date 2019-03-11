package dev.koiki.chord.communitysearch

data class ErrorResponse(
        val trace: String,
        val details: List<ErrorDetail>
)

data class ErrorDetail(
        val code: ErrorCode = ErrorCode.INTERNAL_SERVER_ERROR,
        val message: String
)
