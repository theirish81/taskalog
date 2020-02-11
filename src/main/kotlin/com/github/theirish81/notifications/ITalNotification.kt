package com.github.theirish81.notifications

import com.github.theirish81.messages.TalStatusAndWorklog

interface ITalNotification {

    fun notify(msg : TalStatusAndWorklog)
}