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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data structure for the steps in a task or worklog
 * @param id ID of the step
 * @param status the current status of the step
 * @param expectedStatus the status expected to consider the step concluded
 * @param submissions in worklogs, the list of submissions concerning this step
 */
class TalStep(val id : String,
               var status : String,
               @JsonProperty("expected_status") val expectedStatus : String,
               @JsonInclude(JsonInclude.Include.NON_NULL) var submissions : MutableList<TalSubmission>? = null) {

    /**
     * @return true when status and expectedStatus are equal
     */
    @JsonIgnore
    fun isComplete() : Boolean = status==expectedStatus

}