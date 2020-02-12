package com.github.theirish81.controllers

import com.github.theirish81.actors.TalTimerActors
import com.github.theirish81.messages.TalTimer
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Controller("/timer")
class TimerController {


    @Post("/submit")
    fun submit(@Body talTimer: TalTimer) : HttpResponse<String> {
        GlobalScope.launch {
            TalTimerActors.acceptSubmission!!.send(talTimer)
        }
        return HttpResponse.accepted<String>()
    }
}