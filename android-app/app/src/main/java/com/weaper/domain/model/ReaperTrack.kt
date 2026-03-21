package com.weaper.domain.model

data class ReaperTrack(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val markerId: Int = 0,
    val regionId: Int = 0,
    val duration: String = "",
    val bpm: Int? = null
)
