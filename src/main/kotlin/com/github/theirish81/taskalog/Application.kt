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
package com.github.theirish81.taskalog

import com.github.theirish81.taskalog.actors.TalTaskActors
import com.github.theirish81.taskalog.actors.TalTimerActors
import com.github.theirish81.taskalog.ingresses.TalIngresses
import com.github.theirish81.taskalog.notifications.TalNotifications
import io.micronaut.runtime.Micronaut
import java.util.*

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        TalStatus.bootstrapTime = Date()
        val applicationContext = Micronaut.build()
                                    .packages("com.github.theirish81")
                                    .mainClass(Application.javaClass)
                                    .start()
        TalTaskActors.initialize()
        TalTimerActors.initialize()
        TalTickers.initialize()
        TalIngresses.initialize()
        TalNotifications.initialize()

        Runtime.getRuntime().addShutdownHook(Thread {
            TalTickers.shutdown()
            TalIngresses.shutdown()
            TalNotifications.shutdown()
            applicationContext.close()
        })
    }

}