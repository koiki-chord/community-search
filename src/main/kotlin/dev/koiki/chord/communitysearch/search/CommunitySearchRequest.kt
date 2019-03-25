package dev.koiki.chord.communitysearch.search

data class CommunitySearchRequest(
        val text: String,
        val page: Int,
        val size: Int
)
