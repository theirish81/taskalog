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
import com.github.theirish81.taskalog.messages.TalTimer
import com.github.theirish81.taskalog.messages.TalTimerSubAndWorklog
import com.github.theirish81.taskalog.messages.TalWorklog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.slf4j.LoggerFactory
import java.util.*

/**
 * System tickers
 */
object TalTickers {

    /**
     * A thread pool for tickers
     */
    val pool = newSingleThreadContext("tickersPool")

    /**
     * A timer
     */
    val timer = Timer()

    /**
     * Initializes and starts the tickers
     */
    fun initialize() {
        timer.scheduleAtFixedRate(CheckTaskWorklogs(), TalConfig.getTaskTicker()*1000, TalConfig.getTaskTicker()*1000)
        timer.scheduleAtFixedRate(CheckTimerWorklogs(), TalConfig.getTimerTicker()*1000, TalConfig.getTimerTicker()*1000)
    }

    /**
     * Stops the tickers
     */
    fun shutdown() {
        timer.cancel()
    }
}

/**
 * Ticker task that checks for task worklogs
 */
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

/**
 * Ticker task that checks for timer worklogs
 */
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