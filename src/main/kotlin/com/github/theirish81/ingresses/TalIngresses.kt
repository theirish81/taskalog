package com.github.theirish81.ingresses

object TalIngresses {

    fun initialize() {
        getIngresses().forEach {
            it.start()
        }
    }

    fun shutdown() {
        getIngresses().forEach {
            it.shutdown()
        }
    }

    fun getIngresses() : List<ITalIngress> {
        return (TalConfig.appConfig["ingresses"] as List<String>).map {
            Class.forName(it).kotlin.objectInstance as ITalIngress
        }
    }
}