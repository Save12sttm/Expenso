package com.yourname.expenso.di

import com.yourname.expenso.data.ExportNotificationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ExportNotificationEntryPoint {
    fun exportNotificationManager(): ExportNotificationManager
}