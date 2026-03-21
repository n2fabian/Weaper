package com.weaper.domain.model

data class Playlist(
    val id: String = "",
    val name: String = "",
    val trackIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
