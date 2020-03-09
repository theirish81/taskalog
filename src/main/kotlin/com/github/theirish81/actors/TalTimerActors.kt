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
package com.github.theirish81.actors

import TalConfig
import com.github.theirish81.TalFS
import com.github.theirish81.messages.TalTimer
import com.github.theirish81.messages.TalTimerSubAndWorklog
import com.github.theirish81.notifications.ITalNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Timers actors
 */
object TalTimerActors {

    /**
     * The execution thread pool
     */
    val pool = newSingleThreadContext("timerActorPool")

    /**
     * Actor accepting timer events
     */
    fun CoroutineScope.acceptSubmissionActor() = actor<TalTimer>(pool) {
        val log = LoggerFactory.getLogger("timer.actors.acceptSubmissionActor")
        log.info("Starting actor")
        for(msg in channel) try {
            if(msg.executionTime == null)
                msg.executionTime = Date()
            log.debug("Accepting submission for `${msg.id}`")
            loadTimer!!.send(msg)
        }catch(e : Exception) {
            log.error("Error while accepting submission", e)
        }
    }

    /**
     * Actor loading timer definition
     */
    fun CoroutineScope.loadTimerActor() = actor<TalTimer>(pool){
        val log = LoggerFactory.getLogger("timer.actors.loadTimerActor")
        log.info("Starting actor")
        for(msg in channel) try {
            log.debug("Loading timer `${msg.id}`")
            val timerDefinition = TalFS.loadTimer(msg.id)
            if(timerDefinition.isPresent){
                msg.everySeconds = timerDefinition.get().everySeconds
                loadTimerLog!!.send(msg)
            } else {
                log.error("Could not load timer `${msg.id}`")
            }

        }catch(e : Exception) {
            log.error("Error while accepting submission", e)
        }
    }

    /**
     * Actor that loads ore create a timer log
     */
    fun CoroutineScope.loadOrCreateTimerLogActor() = actor<TalTimer>(pool){
        val log = LoggerFactory.getLogger("timer.actors.loadOrCreateTimerLogActor")
        log.info("Starting actor")
        for(msg in channel) try {
            val worklogFile = TalFS.getTimerWorklogFile(msg.id)
            if(!worklogFile.exists()){
                log.debug("Creating worklog for `${msg.id}`")
                worklogFile.createNewFile()
                worklogFile.deleteOnExit()
                worklogFile.writeText(TalFS.serializeAsYaml(msg))
            } else {
                log.debug("Loading worklog for `${msg.id}`")
                val worklog = TalFS.loadTimerWorklog(msg.id).get()
                val serializableWorklog = worklog.clone()
                serializableWorklog.executionTime = msg.executionTime
                worklogFile.writeText(TalFS.serializeAsYaml(serializableWorklog))
                evaluateTimer!!.send(TalTimerSubAndWorklog(msg,worklog))
            }
        }catch(e : Exception) {
            log.error("Error while accepting submission", e)
        }
    }


    /**
     * Evaluates whether a timer is late or not
     */
    fun CoroutineScope.evaluateTimerActor() = actor<TalTimerSubAndWorklog>(pool){
        val log = LoggerFactory.getLogger("timer.actors.loadOrCreateTimerLogActor")
        log.info("Starting actor")
        for(msg in channel) try {
            if (msg.submission.executionTime!!.time > msg.submission.everySeconds*1000 + msg.worklog.executionTime!!.time) {
                log.debug("Timer is late `${msg.submission.id}`")
                storeResult!!.send(msg.submission)
                notifyTimer!!.send(msg.submission)
                TalFS.getTimerWorklogFile(msg.submission.id).writeText(TalFS.serializeAsYaml(msg.submission))
            } else {
                log.debug("Timer is in time for ${msg.submission.id}`")
            }
        }catch(e : Exception) {
            log.error("Error while accepting submission", e)
        }
    }

    /**
     * The actor that takes care of logging the verdict on the task
     */
    fun CoroutineScope.storeResultActor() = actor<TalTimer>(pool) {
        val log = LoggerFactory.getLogger("timer.actors.storeResultActor")
        val resultsLog = LoggerFactory.getLogger("timer_results")
        for(msg in channel) try {
            log.debug("Registering timer `${msg.id}` result")
            resultsLog.info(TalFS.serializeAsJSON(msg))
        }catch(e : Exception){
            log.error("Error while registering timer", e)
        }
    }


    /**
     * Notifies when a timer is late
     */
    fun CoroutineScope.notifyTimerActor() = actor<TalTimer>(pool) {
        val log = LoggerFactory.getLogger("timer.actors.notifyTimerActorActor")
        log.info("Starting actor")
        for(msg in channel) try {
            log.debug("Running notifications for `${msg.id}`")
            (TalConfig.appConfig["notificators"] as List<String>).forEach {
                val notification = Class.forName(it).kotlin.objectInstance as ITalNotification
                notification.notify(msg)
            }
        }catch(e : Exception){
            log.error("Error in the notification process", e)
        }
    }

    var acceptSubmission : SendChannel<TalTimer>? = null
    var loadTimer : SendChannel<TalTimer>? = null
    var loadTimerLog : SendChannel<TalTimer>? = null
    var evaluateTimer : SendChannel<TalTimerSubAndWorklog>? = null
    var notifyTimer : SendChannel<TalTimer>? = null
    var storeResult : SendChannel<TalTimer>? = null

    fun initialize() {
        GlobalScope.launch(TalTaskActors.pool) {
            acceptSubmission = acceptSubmissionActor()
            loadTimer = loadTimerActor()
            loadTimerLog = loadOrCreateTimerLogActor()
            evaluateTimer = evaluateTimerActor()
            notifyTimer = notifyTimerActor()
            storeResult = storeResultActor()
        }
    }
}