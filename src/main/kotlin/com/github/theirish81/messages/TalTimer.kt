package com.github.theirish81.messages

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class TalTimer(val id : String,
                    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("within_seconds") var everySeconds : Int,
                    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("execution_time") var executionTime : Date? = null)