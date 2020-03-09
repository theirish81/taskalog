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

import com.github.theirish81.messages.TalStatusAndWorklog
import com.github.theirish81.messages.TalTimer

/**
 * Interface for notifications
 */
interface ITalNotification {

    /**
     * Initialization routine
     */
    fun initialize()

    /**
     * Triggers a notification for a worklog
     * @param msg the worklog
     */
    fun notify(msg : TalStatusAndWorklog)

    /**
     * Triggers a notification for a time
     * @param msg the timer
     */
    fun notify(msg : TalTimer)

    /**
     * Shuts the notification down
     */
    fun shutdown()
}