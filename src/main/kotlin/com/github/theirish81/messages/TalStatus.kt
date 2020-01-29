package com.github.theirish81.messages

import java.util.*

/**
 * The general status of a worklog
 * @param complete true when the worklog is completed
 * @param late true when the worklog is late
 */
data class TalStatus(val complete : Boolean,
                     val late : Boolean,
                     val valid : Boolean = true,
                     val details : String = "",
                     val evaluationDate : Date = Date())