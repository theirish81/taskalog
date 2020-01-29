package com.github.theirish81.messages

/**
 * Bean carrying a submission and a task
 * @param submission the submission
 * @param task the task
 */
data class TalSubAndTask(val submission: TalSubmission, var task: TalTask)