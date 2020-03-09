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
package com.github.theirish81.messages

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * A worklog
 * @param id unique identifier of the worklog
 * @param taskId id of the task a worklog derives from
 * @param maxDurationSeconds seconds after which a derived worklog is to be considered "late"
 * @param steps the steps of the task
 * @param creationDate the date the worklog was created
 * @param updateDate the date the worklog was updated
 */
class TalWorklog(val id : String,
                 taskId: String,
                 maxDurationSeconds: Int,
                 steps: List<TalStep>,
                 @JsonInclude(JsonInclude.Include.NON_NULL) ordered : Boolean = false,
                 @JsonProperty("creation_date") val creationDate: Date,
                 @JsonProperty("update_date") var updateDate: Date?) : TalTask(taskId, maxDurationSeconds, steps, ordered) {

    constructor(id : String, talTask: TalTask) :
        this(id,talTask.taskId,talTask.maxDurationSeconds,talTask.steps,talTask.ordered,Date(),Date())

    /**
     * @return true if all steps have completed
     */
    @JsonIgnore
    fun isComplete() : Boolean {
        for (s in steps){
            if (!s.isComplete())
                return false
        }
        return true
    }

    /**
     * @return the time between the creation date and now, in milliseconds
     */
    @JsonIgnore
    fun getProcessTimeMillis() : Long = System.currentTimeMillis() - creationDate!!.time

    /**
     * @return true if the worklog is late
     */
    @JsonIgnore
    fun isLate() : Boolean = getProcessTimeMillis() > maxDurationSeconds*1000

    @JsonIgnore
    fun isInOrder() : Boolean  {
        if(ordered){
            var previousCompletion = "_"
            for(step in steps){
                if(previousCompletion == "false" && step.isComplete())
                    return false
                previousCompletion = step.isComplete().toString()
            }
        }
        return true
    }
}