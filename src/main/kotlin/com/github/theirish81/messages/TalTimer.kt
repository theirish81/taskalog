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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * Timer and timer submission message
 * @param id the ID of the timer
 * @param everySeconds within which time span the event should happen
 * @param executionTime the time this timer executed
 */
class TalTimer(val id : String,
                    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("within_seconds") var everySeconds : Int,
                    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("execution_time") var executionTime : Date? = null){

    fun clone() : TalTimer = TalTimer(id,everySeconds,executionTime)
}