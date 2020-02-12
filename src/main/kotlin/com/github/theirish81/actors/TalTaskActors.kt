package com.github.theirish81.actors

import com.github.theirish81.TalFS
import com.github.theirish81.messages.*
import com.github.theirish81.notifications.ITalNotification
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Actors of Task-a-Log
 */
object TalTaskActors {

    /**
     * Executor pool with a single thread in it
     */
    val pool = newSingleThreadContext("taskActorPool")

    /**
     * The actor accepting submissions and registering the date or reception
     */
    fun CoroutineScope.acceptSubmissionActor() = actor<TalSubmission>(pool) {
        val log = LoggerFactory.getLogger("task.actors.acceptSubmissionActor")
        log.info("Starting actor")
        for(msg in channel) try {
            log.debug("Accepting submission for `${msg.id}`")
            msg.receiveDate = Date()
            loadTask?.send(msg)
        }catch (e : Exception) {
            log.error("Error while accepting submission", e)
        }
    }

    /**
     * The actor loading the appropriate task for a submission
     */
    fun CoroutineScope.loadTaskActor() = actor<TalSubmission>(pool) {
        val log = LoggerFactory.getLogger("task.actors.loadTaskActor")
        log.info("Starting actor")
        for(msg in channel) try {
            log.debug("Loading task `${msg.taskId}` for `${msg.id}`")
            val task = TalFS.loadTask(msg.taskId)
            if(task.isPresent) {
                val subAndTask = TalSubAndTask(msg, task.get())
                findOrCreateWorklog?.send(subAndTask)
            } else {
                log.error("Error - I couldn't find the task")
            }
        }catch(e : Exception) {
            log.error("Error while loading task", e)
        }
    }

    /**
     * The actor loading or creating a worklog for the submission
     */
    fun CoroutineScope.findOrCreateWorklogActor() = actor<TalSubAndTask>(pool) {
        val log = LoggerFactory.getLogger("task.actors.findOrCreateWorklogActor")
        log.info("Starting actor")
        for(msg in channel)try {
            if(TalFS.hasWorklogExpired(msg.task.taskId,msg.submission.id)){
                log.debug("Submission to expired or invalid worklog `${msg.submission.id}`. Discarding")
                continue
            }
            val worklogFile = TalFS.getTaskWorklogFile(msg.task.taskId,msg.submission.id)
            if(!worklogFile.exists()) {
                log.debug("Creating new worklog file for `${msg.submission.id}`")
                val worklog = TalWorklog(msg.submission.id,msg.task)
                worklogFile.createNewFile()
                worklogFile.writeText(TalFS.serializeAsYaml(worklog))
                updateWorklog?.send(TalSubAndWorklog(msg.submission,worklog))
            } else {
                log.debug("Loading existing worklog file for `${msg.submission.id}`")
                updateWorklog?.send(TalSubAndWorklog(msg.submission,TalFS.deserializeYaml(worklogFile,TalWorklog::class.java)))
            }
        }catch(e : Exception) {
            log.error("Error during find/create worklog",e)
        }

    }

    /**
     * The actor updating a worklog with the submission
     */
    fun CoroutineScope.updateWorklogActor() = actor<TalSubAndWorklog>(pool) {
        val log = LoggerFactory.getLogger("task.actors.updateWorklogActor")
        log.info("Starting actor")
        for(msg in channel) try {
            val step = msg.worklog.steps.find { it.id == msg.submission.stepId }
            if(step == null){
                log.error("Error, step `${msg.submission.stepId}` does not exist in task `${msg.submission.taskId}` for `${msg.submission.id}`")
            } else {
                log.debug("Registering submission for `${msg.submission.id}`")
                step.status = msg.submission.status
                if(step.submissions == null)
                    step.submissions = mutableListOf(msg.submission)
                else
                    step.submissions!!.add(msg.submission)
            }
            val taskFile = TalFS.getTaskWorklogFile(msg.worklog.taskId, msg.submission.id)
            taskFile.writeText(TalFS.serializeAsYaml(msg.worklog))
            evaluateWorklog?.send(msg.worklog)
        }catch(e : Exception){
            log.error("Error while updating worklog", e)
        }
    }

    /**
     * The actor determining the status of worklog
     */
    fun CoroutineScope.evaluateWorklogActor() = actor<TalWorklog>(pool) {
        val log = LoggerFactory.getLogger("task.actors.evaluateWorklogActor")
        log.info("Starting actor")
        for(msg in channel)try {
            val status = TalStatus(msg.isComplete(),msg.isLate(),msg.isInOrder(),
                                    if(!msg.isInOrder()) "NOT_ORDERED" else "")
            log.debug("Evaluating the status of `${msg.id}` -> Complete: ${status.complete}, Late: ${status.late}, InOrder: ${status.valid}")
            if(status.complete || status.late || !status.valid) {
                val statusAndWorklog = TalStatusAndWorklog(status,msg)
                cleanup?.send(statusAndWorklog)
                storeResult!!.send(statusAndWorklog)
                if(status.late || !status.valid)
                    notify?.send(statusAndWorklog)
            }
        }catch (e : Exception){
            log.error("Error during worklog evaluation",e)
        }
    }

    /**
     * The actor that takes care of logging the verdict on the task
     */
    fun CoroutineScope.storeResultActor() = actor<TalStatusAndWorklog>(pool) {
        val log = LoggerFactory.getLogger("task.actors.storeResultActor")
        val resultsLog = LoggerFactory.getLogger("results")
        for(msg in channel) try {
            log.debug("Registering worklog `${msg.worklog.id}` result")
            resultsLog.info(TalFS.serializeAsJSON(msg))
        }catch(e : Exception){
            log.error("Error while registering worklog", e)
        }
    }

    /**
     * The actor triggering notifications based on the worklog status
     */
    fun CoroutineScope.notifyActor() = actor<TalStatusAndWorklog>(pool) {
        val log = LoggerFactory.getLogger("task.actors.notifyActor")

        log.info("Starting actor")
        for(msg in channel) try {

            if(msg.status.late || !msg.status.valid) {
                log.debug("Running notifiers for `${msg.worklog.id}`")
                (TalConfig.appConfig["notificators"] as List<String>).forEach {
                    val notification = Class.forName(it).kotlin.objectInstance as ITalNotification
                    notification.notify(msg)
                }
            }
        }catch(e : Exception) {
            log.error("Error in the notification process", e)
        }
    }

    /**
     * The actor cleaning up the expired/finished worklogs
     */
    fun CoroutineScope.cleanupActor() = actor<TalStatusAndWorklog>(pool) {
        val log = LoggerFactory.getLogger("task.actors.cleanupActor")
        log.info("Starting actor")
        for(msg in channel) try {
            val worklogFile = TalFS.getTaskWorklogFile(msg.worklog.taskId,msg.worklog.id)
            if(msg.status.late || !msg.status.valid) {
                log.debug("Creating expiry file for worklog `${msg.worklog.id}`")
                TalFS.createExpiryFile(msg.worklog.taskId, msg.worklog.id)
            }
            log.debug("Deleting worklog `${msg.worklog.id}`")
            worklogFile.delete()
        }catch(e : Exception) {
            log.error("Error during cleanup", e)
        }
    }



    var acceptSubmission : SendChannel<TalSubmission>? = null
    var loadTask  : SendChannel<TalSubmission>? = null
    var findOrCreateWorklog : SendChannel<TalSubAndTask>? = null
    var updateWorklog : SendChannel<TalSubAndWorklog>? = null
    var evaluateWorklog : SendChannel<TalWorklog>? = null
    var storeResult : SendChannel<TalStatusAndWorklog>? = null
    var notify : SendChannel<TalStatusAndWorklog>? = null
    var cleanup : SendChannel<TalStatusAndWorklog>? = null

    /**
     * Initializes the actors
     */
    fun initialize()  {
        GlobalScope.launch(pool) {
            acceptSubmission = acceptSubmissionActor()
            loadTask = loadTaskActor()
            findOrCreateWorklog = findOrCreateWorklogActor()
            updateWorklog = updateWorklogActor()
            evaluateWorklog = evaluateWorklogActor()
            storeResult = storeResultActor()
            notify = notifyActor()
            cleanup = cleanupActor()
        }
    }
}