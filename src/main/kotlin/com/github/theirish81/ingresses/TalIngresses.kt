package com.github.theirish81.ingresses

object TalIngresses {

    fun initialize() {
        (TalConfig.appConfig["ingresses"] as List<String>).forEach {
            val ingress = Class.forName(it).kotlin.objectInstance as ITalIngress
            ingress.start()
        }
    }
}