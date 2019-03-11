package dev.koiki.chord.communitysearch

data class CommunitySearchRequest(
        val text: String? = null
) {
    fun isValid() {
        if (text != null && text.isBlank())
            throw ValidationException(listOf(
                    ErrorDetail(
                            code = ErrorCode.TEXT_SHOULD_NOT_BE_BLANK,
                            message = "text should not be blank"
                    )
            ))
    }
}
