package com.github.theirish81

import com.github.theirish81.actors.TalTaskActors
import com.github.theirish81.actors.TalTimerActors
import com.github.theirish81.messages.TalTimer
import com.github.theirish81.messages.TalTimerSubAndWorklog
import com.github.theirish81.messages.TalWorklog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.util.*


object TalTickers {

    val pool = newSingleThreadContext("tickersPool")

    val timer = Timer()

    fun init() {
        timer.scheduleAtFixedRate(CheckTaskWorklogs(),10000,10000)
        timer.scheduleAtFixedRate(CheckTimerWorklogs(),10000,10000)
    }
}

class CheckTaskWorklogs : TimerTask() {
    override fun run() {
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
    override fun run() {
        TalFS.getTimerWorklogsFile().listFiles().filter { it.extension == "yml" }.forEach { worklog ->
            GlobalScope.launch(TalTickers.pool) {
                val timerWorklog = TalFS.deserializeYaml(worklog, TalTimer::class.java)
                TalTimerActors.evaluateTimer!!.send(TalTimerSubAndWorklog(TalTimer(timerWorklog.id,timerWorklog.everySeconds,Date()),timerWorklog))
            }
        }
    }
}