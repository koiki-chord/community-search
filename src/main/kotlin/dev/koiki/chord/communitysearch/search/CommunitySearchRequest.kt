package dev.koiki.chord.communitysearch.search

data class CommunitySearchRequest(
        val text: String,
        val offset: Int,
        val limit: Int
)
