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
package com.github.theirish81.taskalog.messages

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * A submission. This data structure is forwarded by an external agent, when an action is completed, as part of a
 * broader task
 * @param id the unique ID of the broader task. Any submission belonging to the same task need to carry the same ID
 * @param taskId the task ID
 * @param stepId the ID of the step within the task definition
 * @param senderId the ID of the sender
 * @param status a string representing the result of the action that caused the submission
 * @param meta further meta information
 * @param creationDate a date representing the time the submission was created by the sender
 * @param receiveDate a date representing the time Task-a-Log received the submission
 */
data class TalSubmission(val id : String,
                         @JsonProperty("task_id") val taskId : String,
                         @JsonProperty("step_id") val stepId : String,
                         @JsonProperty("sender_id") val senderId : String,
                         val status : String,
                         @JsonProperty("meta") @JsonInclude(JsonInclude.Include.NON_NULL) val meta : Map<String,*> = HashMap<String,Any>(),
                         @JsonProperty("creation_date") @JsonInclude(JsonInclude.Include.NON_NULL) val creationDate : Date = Date(),
                         @JsonProperty("receive_date") @JsonInclude(JsonInclude.Include.NON_NULL) var receiveDate : Date? = Date())