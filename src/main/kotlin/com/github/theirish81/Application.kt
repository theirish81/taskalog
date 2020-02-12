package com.github.theirish81

import com.github.theirish81.actors.TalTaskActors
import com.github.theirish81.actors.TalTimerActors
import io.micronaut.runtime.Micronaut
import java.util.*

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        TalStatus.bootstrapTime = Date()
        Micronaut.build()
                .packages("com.github.theirish81")
                .mainClass(Application.javaClass)
                .start()
        TalTaskActors.initialize()
        TalTimerActors.initialize()
        TalTickers.init()
    }

}