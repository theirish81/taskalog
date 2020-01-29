package com.github.theirish81.messages

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