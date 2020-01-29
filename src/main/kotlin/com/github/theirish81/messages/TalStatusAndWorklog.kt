package com.github.theirish81.messages

/**
 * A bean carrying both the status and the worklog itself
 * @param status the status of the worklog
 * @param worklog the worklog
 */
data class TalStatusAndWorklog(val status : TalStatus, val worklog : TalWorklog)