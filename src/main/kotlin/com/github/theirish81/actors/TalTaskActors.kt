package com.github.theirish81.actors

import com.github.theirish81.TalFS
import com.github.theirish81.messages.*
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
    val pool = newSingleThreadContext("actorPool")

    /**
     * The actor accepting submissions and registering the date or reception
     */
    fun CoroutineScope.acceptSubmissionActor() = actor<TalSubmission>(pool) {
        val log = LoggerFactory.getLogger("actors.acceptSubmissionActor")
        log.info("Starting actor")
        for(msg in channel){
            log.debug("Accepting submission for `${msg.id}`")
            msg.receiveDate = Date()
            loadTask?.send(msg)
        }
    }

    /**
     * The actor loading the appropriate task for a submission
     */
    fun CoroutineScope.loadTaskActor() = actor<TalSubmission>(pool) {
        val log = LoggerFactory.getLogger("actors.loadTaskActor")
        log.info("Starting actor")
        for(msg in channel){
            log.debug("Loading task `${msg.taskId}` for `${msg.id}`")
            val task = TalFS.loadTask(msg.taskId)
            if(task.isPresent) {
                val subAndTask = TalSubAndTask(msg, task.get())
                findOrCreateWorklog?.send(subAndTask)
            } else {
                log.error("Error - I couldn't find the task")
            }
        }
    }

    /**
     * The actor loading or creating a worklog for the submission
     */
    fun CoroutineScope.findOrCreateWorklogActor() = actor<TalSubAndTask>(pool) {
        val log = LoggerFactory.getLogger("actors.findOrCreateWorklogActor")
        log.info("Starting actor")
        for(msg in channel){
            if(TalFS.hasWorklogExpired(msg.task.taskId,msg.submission.id)){
                log.debug("Submission to expired or invalid worklog `${msg.submission.id}`. Discarding")
                continue
            }
            val worklogFile = TalFS.getWorklogFile(msg.task.taskId,msg.submission.id)
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
        }

    }

    /**
     * The actor updating a worklog with the submission
     */
    fun CoroutineScope.updateWorklogActor() = actor<TalSubAndWorklog>(pool) {
        val log = LoggerFactory.getLogger("actors.updateWorklogActor")
        log.info("Starting actor")
        for(msg in channel){
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
            val taskFile = TalFS.getWorklogFile(msg.worklog.taskId, msg.submission.id)
            taskFile.writeText(TalFS.serializeAsYaml(msg.worklog))
            evaluateWorklog?.send(msg.worklog)
        }
    }

    /**
     * The actor determining the status of worklog
     */
    fun CoroutineScope.evaluateWorklogActor() = actor<TalWorklog>(pool) {
        val log = LoggerFactory.getLogger("actors.evaluateWorklogActor")
        log.info("Starting actor")
        for(msg in channel){
            val status = TalStatus(msg.isComplete(),msg.isLate(),msg.isInOrder(),
                                    if(!msg.isInOrder()) "NOT_ORDERED" else "")
            log.debug("Evaluating the status of `${msg.id}` -> Complete: ${status.complete}, Late: ${status.late} InOrder: ${status.valid}")
            val statusAndWorklog = TalStatusAndWorklog(status,msg)
            notify?.send(statusAndWorklog)
        }
    }

    /**
     * The actor triggering notifications based on the worklog status
     */
    fun CoroutineScope.notifyActor() = actor<TalStatusAndWorklog>(pool) {
        val log = LoggerFactory.getLogger("actors.notifyActor")
        val resultsLog = LoggerFactory.getLogger("results")
        log.info("Starting actor")
        for(msg in channel){
            if(msg.status.complete || msg.status.late || !msg.status.valid) {
                resultsLog.info(TalFS.serializeAsJSON(msg))
                cleanup?.send(msg)
            }
        }
    }

    /**
     * The actor cleaning up the expired/finished worklogs
     */
    fun CoroutineScope.cleanupActor() = actor<TalStatusAndWorklog>(pool) {
        val log = LoggerFactory.getLogger("actors.cleanupActor")
        log.info("Starting actor")
        for(msg in channel){
            val worklogFile = TalFS.getWorklogFile(msg.worklog.taskId,msg.worklog.id)
            if(msg.status.late || !msg.status.valid)
                TalFS.createExpiryFile(msg.worklog.taskId,msg.worklog.id)
            worklogFile.delete()
        }
    }



    var acceptSubmission : SendChannel<TalSubmission>? = null
    var loadTask  : SendChannel<TalSubmission>? = null
    var findOrCreateWorklog : SendChannel<TalSubAndTask>? = null
    var updateWorklog : SendChannel<TalSubAndWorklog>? = null
    var evaluateWorklog : SendChannel<TalWorklog>? = null
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
            notify = notifyActor()
            cleanup = cleanupActor()
        }
    }
}