package com.github.theirish81.notifications

import TalConfig

object TalNotifications {

    fun initialize() {
        getNotificators().forEach {
            it.initialize()
        }
    }

    fun shutdown() {
        getNotificators().forEach {
            it.shutdown()
        }
    }

    fun getNotificators() : List<ITalNotification> {
        return (TalConfig.appConfig["notificators"] as List<String>).map {
            Class.forName(it).kotlin.objectInstance as ITalNotification
        }
    }
}