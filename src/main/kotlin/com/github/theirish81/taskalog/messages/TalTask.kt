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

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A task definition
 * @param taskId the ID of the task
 * @param maxDurationSeconds seconds after which a derived worklog is to be considered "late"
 * @param steps the steps of the task
 * @param ordered true if the steps should appear in order
 */
@JsonFormat(shape=JsonFormat.Shape.OBJECT)
open class TalTask(@JsonProperty("task_id") val taskId : String,
                   @JsonProperty("max_duration_seconds") val maxDurationSeconds : Int,
                   val steps : List<TalStep>,
                   @JsonInclude(JsonInclude.Include.NON_NULL) val ordered: Boolean = false)