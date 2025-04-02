package com.yourfitness.coach.domain.spike

import com.spikeapi.SpikeLogger
import timber.log.Timber

class Logger : SpikeLogger {
    override fun debug(message: String) {
        Timber.tag("SpikeLogger").d(message)
    }

    override fun error(message: String) {
        Timber.tag("SpikeLogger").e(message)
    }

    override fun info(message: String) {
        Timber.tag("SpikeLogger").i(message)
    }

    override fun isDebugEnabled(): Boolean {
        return true
    }

    override fun isErrorEnabled(): Boolean {
        return true
    }

    override fun isInfoEnabled(): Boolean {
        return true
    }
}
