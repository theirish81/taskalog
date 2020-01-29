package com.github.theirish81.messages

/**
 * A bean carrying a submission and a worklog
 * @param submission a submission
 * @param worklog a worklog
 */
data class TalSubAndWorklog(val submission: TalSubmission, val worklog: TalWorklog)