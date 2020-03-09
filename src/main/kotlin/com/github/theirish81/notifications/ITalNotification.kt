package com.github.theirish81.notifications

import com.github.theirish81.messages.TalStatusAndWorklog
import com.github.theirish81.messages.TalTimer

interface ITalNotification {

    fun initialize()

    fun notify(msg : TalStatusAndWorklog)

    fun notify(msg : TalTimer)

    fun shutdown()
}