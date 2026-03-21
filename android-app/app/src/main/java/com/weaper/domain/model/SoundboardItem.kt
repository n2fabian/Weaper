package com.weaper.domain.model

data class SoundboardItem(
    val id: String = "",
    val label: String = "",
    val fileName: String = "",
    val fileHash: String = "",
    val trackId: Int = 0,
    val reaperSlot: Int = 0,
    val oscPath: String = "",
    val color: Long = 0xFF1E88E5,
    val isAvailable: Boolean = false,
    val orderIndex: Int = 0
)
