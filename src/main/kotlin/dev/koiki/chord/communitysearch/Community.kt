package dev.koiki.chord.communitysearch

data class Community(
        val name: String,
        val desc: String,
        val tags: List<Tag>
)

data class Tag(
        val value: String
)
