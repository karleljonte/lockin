package com.example.lockin

import java.io.Serializable

data class Task(
    var title: String,
    var time: String,
    var status: String,
    var isMuted: Boolean = false,
    val id: Long = System.currentTimeMillis()
) : Serializable
