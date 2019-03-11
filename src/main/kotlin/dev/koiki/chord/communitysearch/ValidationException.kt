package dev.koiki.chord.communitysearch

import java.lang.Exception

class ValidationException: Exception {
    val errorDetails: List<ErrorDetail>

    constructor(errorDetails: List<ErrorDetail>) {
        this.errorDetails = errorDetails
    }
}
