package com.github.theirish81.taskalog

object TalConfig {

    val appConfig : Map<*,*> = TalFS.parseYamlFile(TalFS.getEtcFile().resolve("application.yml"))

    fun getTickers() : Map<String,Long> = appConfig["tickers"] as Map<String,Long>

    fun getTaskTicker() : Long = getTickers()["tasks_seconds"]!!

    fun getTimerTicker() : Long = getTickers()["timers_seconds"]!!
}