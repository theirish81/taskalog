package com.github.theirish81.messages

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.theirish81.messages.TalStep
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

/**
 * A task definition
 * @param taskId the ID of the task
 * @param maxDurationSeconds seconds after which a derived worklog is to be considered "late"
 * @param steps the steps of the task
 */
@JsonFormat(shape=JsonFormat.Shape.OBJECT)
open class TalTask(@JsonProperty("task_id") val taskId : String,
                   @JsonProperty("max_duration_seconds") val maxDurationSeconds : Int,
                   val steps : List<TalStep>,
                   @JsonInclude(JsonInclude.Include.NON_NULL) val ordered: Boolean = false)