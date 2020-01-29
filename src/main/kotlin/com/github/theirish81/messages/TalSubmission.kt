package com.github.theirish81.messages

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * A submission. This data structure is forwarded by an external agent, when an action is completed, as part of a
 * broader task
 * @param id the unique ID of the broader task. Any submission belonging to the same task need to carry the same ID
 * @param stepId the ID of the step within the task definition
 * @param senderId the ID of the sender
 * @param status a string representing the result of the action that caused the submission
 * @param creationDate a date representing the time the submission was created by the sender
 * @param receiveDate a date representing the time Task-a-Log received the submission
 */
data class TalSubmission(val id : String,
                         @JsonProperty("task_id") val taskId : String,
                         @JsonProperty("step_id") val stepId : String,
                         @JsonProperty("sender_id") val senderId : String,
                         val status : String,
                         @JsonProperty("creation_date") @JsonInclude(JsonInclude.Include.NON_NULL) val creationDate : Date = Date(),
                         @JsonProperty("receive_date") @JsonInclude(JsonInclude.Include.NON_NULL) var receiveDate : Date? = Date())