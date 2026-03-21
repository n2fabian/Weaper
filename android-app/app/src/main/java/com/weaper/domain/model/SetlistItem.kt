package com.weaper.domain.model

data class SetlistItem(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val markerId: Int = 0,
    val regionId: Int = 0,
    val autoPlay: Boolean = true,
    val bpm: Int? = null,
    val notes: String = "",
    val orderIndex: Int = 0
)
