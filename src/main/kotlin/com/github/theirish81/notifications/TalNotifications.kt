/*
 *   Copyright 2020 Simone Pezzano
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   @author Simone Pezzano
 *
 */
package com.github.theirish81.notifications

import TalConfig

/**
 * The object handling the initialization of the notifications
 */
object TalNotifications {

    /**
     * Initializes all notification implementations
     */
    fun initialize() {
        getNotificators().forEach {
            it.initialize()
        }
    }

    /**
     * Shuts down all notification implementation
     */
    fun shutdown() {
        getNotificators().forEach {
            it.shutdown()
        }
    }

    /**
     * Gets all notification implementations
     * @return a list of all notification implementations
     */
    fun getNotificators() : List<ITalNotification> {
        return (TalConfig.appConfig["notificators"] as List<String>).map {
            Class.forName(it).kotlin.objectInstance as ITalNotification
        }
    }
}