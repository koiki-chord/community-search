package dev.koiki.chord.communitysearch.search

import dev.koiki.chord.communitysearch.Community

data class CommunitySearchResult(
        val communities: List<Community>,
        val meta: Meta
)

data class Meta(
        val offset: Int,
        val limit: Int,
        val totalHits: Long
)