package com.yourfitness.coach.domain.helpers

import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MutexHelper @Inject constructor() {
    val syncAchievementsMutex = Mutex()
    val remoteConfigMutex = Mutex()

    val openConnectionMutex = Mutex()
    val closeConnectionMutex = Mutex()
}