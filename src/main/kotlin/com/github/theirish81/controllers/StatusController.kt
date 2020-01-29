package com.github.theirish81.controllers

import com.github.theirish81.TalStatus
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