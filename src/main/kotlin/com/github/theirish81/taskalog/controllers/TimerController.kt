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
package com.github.theirish81.taskalog.controllers

import com.github.theirish81.taskalog.actors.TalTimerActors
import com.github.theirish81.taskalog.messages.TalTimer
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The controller for timer submissions
 */
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