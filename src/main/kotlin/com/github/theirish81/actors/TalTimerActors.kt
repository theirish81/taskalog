package com.github.theirish81.actors

import com.github.theirish81.TalFS
import com.github.theirish81.messages.TalTimer
import com.github.theirish81.messages.TalTimerAndLastExecutionTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.slf4j.LoggerFactory
import java.util.*

object TalTimerActors {

    val pool = newSingleThreadContext("timerActorPool")


    fun CoroutineScope.acceptSubmissionActor() = actor<TalTimer>(TalTimerActors.pool) {
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

    fun CoroutineScope.loadTimerActor() = actor<TalTimer>(TalTimerActors.pool){
        val log = LoggerFactory.getLogger("timer.actors.loadTimerActor")
        log.info("Starting actor")
        for(msg in channel) try {
            log.debug("Loading timer `${msg.id}`")
            val timerDefinition = TalFS.loadTimer(msg.id)
            if(timerDefinition.isPresent){
                msg.everySeconds = timerDefinition.get().everySeconds
                loadTimerLog!!.send(msg)
            }

        }catch(e : Exception) {
            log.error("Error while accepting submission", e)
        }
    }

    fun CoroutineScope.loadOrCreateTimerLogActor() = actor<TalTimer>(TalTimerActors.pool){
        val log = LoggerFactory.getLogger("timer.actors.loadOrCreateTimerLogActor")
        log.info("Starting actor")
        for(msg in channel) try {
            val worklogFile = TalFS.getTimerWorklogFile(msg.id)
            if(!worklogFile.exists()){
                log.debug("Creating worklog for `${msg.id}`")
                worklogFile.createNewFile()
                worklogFile.writeText(TalFS.serializeAsYaml(msg))

            } else {
                log.debug("Loading worklog for `${msg.id}`")
                val lastExecutionTime = TalFS.loadTimerWorklog(msg.id).get().executionTime!!.time
                evaluateTimer!!.send(TalTimerAndLastExecutionTime(msg,lastExecutionTime))
                worklogFile.writeText(TalFS.serializeAsYaml(msg))
            }
        }catch(e : Exception) {
            log.error("Error while accepting submission", e)
        }
    }

    fun CoroutineScope.evaluateTimerActor() = actor<TalTimerAndLastExecutionTime>(TalTimerActors.pool){
        val log = LoggerFactory.getLogger("timer.actors.loadOrCreateTimerLogActor")
        log.info("Starting actor")
        for(msg in channel) try {
            if (msg.timer.executionTime!!.time > msg.lastExecutionTime+ + msg.timer.everySeconds*1000) {
                println("BAD")
            }
        }catch(e : Exception) {
            log.error("Error while accepting submission", e)
        }
    }

    var acceptSubmission : SendChannel<TalTimer>? = null
    var loadTimer : SendChannel<TalTimer>? = null
    var loadTimerLog : SendChannel<TalTimer>? = null
    var evaluateTimer : SendChannel<TalTimerAndLastExecutionTime>? = null

    fun initialize() {
        GlobalScope.launch(TalTaskActors.pool) {
            acceptSubmission = acceptSubmissionActor()
            loadTimer = loadTimerActor()
            loadTimerLog = loadOrCreateTimerLogActor()
            evaluateTimer = evaluateTimerActor()
        }
    }
}