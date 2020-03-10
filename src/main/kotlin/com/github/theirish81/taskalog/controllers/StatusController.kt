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

import com.github.theirish81.taskalog.TalStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces

/**
 * The controller to exposing the status of the system
 */
@Controller("/status")
class StatusController {

    /**
     * Returns info about the system
     * @return map representing the status of the system
     */
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun index() : Map<String, Any> {
        return mapOf("status"        to "OK",
                     "bootstrapTime" to TalStatus.bootstrapTime)
    }

}