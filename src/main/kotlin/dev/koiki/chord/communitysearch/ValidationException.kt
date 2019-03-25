package dev.koiki.chord.communitysearch

import java.lang.RuntimeException

class ValidationException(
        val code: ErrorCode,
        override val message: String
) : RuntimeException(message)
