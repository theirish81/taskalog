package com.github.theirish81

import com.github.theirish81.actors.TalTaskActors
import com.github.theirish81.actors.TalTimerActors
import com.github.theirish81.ingresses.TalIngresses
import com.github.theirish81.messages.TalTimer
import com.github.theirish81.notifications.TalNotifications
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