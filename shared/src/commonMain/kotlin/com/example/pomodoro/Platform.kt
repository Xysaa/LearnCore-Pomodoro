package com.example.pomodoro

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
