package dev.koiki.chord.communitysearch

enum class ErrorCode {
    INTERNAL_SERVER_ERROR,
    TEXT_SHOULD_NOT_BE_BLANK
    ;

    companion object {
        fun convert(name: String?): ErrorCode =
                if (name == null)
                    ErrorCode.INTERNAL_SERVER_ERROR
                else
                    ErrorCode.values()
                            .filter { name == it.name }
                            .first()
    }
}
