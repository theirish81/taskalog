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

import java.util.*

/**
 * The general status of a worklog
 * @param complete true when the worklog is completed
 * @param late true when the worklog is late
 * @param valid true when the worklog is valid
 * @param details further details describing the reason for the worklog to be invalid
 * @param evaluationDate the date in which the worklog was evaluated and this status was created
 */
data class TalStatus(val complete : Boolean,
                     val late : Boolean,
                     val valid : Boolean = true,
                     val details : String = "",
                     val evaluationDate : Date = Date())