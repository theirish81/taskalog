package com.github.theirish81

import TalConfig
import com.github.theirish81.actors.TalTaskActors
import com.github.theirish81.actors.TalTimerActors
import com.github.theirish81.messages.TalTimer
import com.github.theirish81.messages.TalTimerSubAndWorklog
import com.github.theirish81.messages.TalWorklog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.slf4j.LoggerFactory
import java.util.*


object TalTickers {

    val pool = newSingleThreadContext("tickersPool")

    val timer = Timer()

    fun init() {
        timer.scheduleAtFixedRate(CheckTaskWorklogs(),TalConfig.getTaskTicker()*1000,TalConfig.getTaskTicker()*1000)
        timer.scheduleAtFixedRate(CheckTimerWorklogs(),TalConfig.getTimerTicker()*1000,TalConfig.getTimerTicker()*1000)
    }
}

class CheckTaskWorklogs : TimerTask() {
    val log = LoggerFactory.getLogger(CheckTaskWorklogs::class.java)
    override fun run() {
        log.debug("Tick")
        TalFS.getTaskWorklogsFile().listFiles().filter{ it.isDirectory }.forEach { category ->
            category.listFiles().filter{ it.extension == "yml" }.forEach { worklog ->
                GlobalScope.launch(TalTickers.pool) {
                    TalTaskActors.evaluateWorklog!!.send(TalFS.deserializeYaml(worklog,TalWorklog::class.java))
                }
            }
        }
    }
}

class CheckTimerWorklogs : TimerTask() {
    val log = LoggerFactory.getLogger(CheckTimerWorklogs::class.java)
    override fun run() {
        log.debug("Tick")
        TalFS.getTimerWorklogsFile().listFiles().filter { it.extension == "yml" }.forEach { worklog ->
            GlobalScope.launch(TalTickers.pool) {
                val timerWorklog = TalFS.deserializeYaml(worklog, TalTimer::class.java)
                TalTimerActors.evaluateTimer!!.send(TalTimerSubAndWorklog(TalTimer(timerWorklog.id,timerWorklog.everySeconds,Date()),timerWorklog))
            }
        }
    }
}