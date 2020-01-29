package com.github.theirish81.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.theirish81.TalFS
import com.github.theirish81.actors.TalTaskActors
import com.github.theirish81.messages.TalSubmission
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The controller for worklogs
 */
@Controller("/worklog")
class WorklogController {

    companion object {
        /**
         * JSON Object Mapper
         */
        private val objectMapper : ObjectMapper = ObjectMapper()
        /**
         * YAML content type
         */
        private val CONTENT_TYPE_X_YAML =  "application/x-yaml"
        /**
         * Alternative YAML content type
         */
        private val CONTENT_TYPE_TEXT_YAML = "text/yaml"
        /**
         * JSON content type
         */
        private val CONTENT_TYPE_APPLICATION_JSON = "application/json"
    }

    /**
     * Submits a TalSubmission
     * @param submission a TalSubmission, passed as request body
     * @return empty body with "accepted" status code
     */
    @Post("/submit")
    fun submit(@Body submission : TalSubmission) : HttpResponse<String> {
        GlobalScope.launch {
            TalTaskActors.acceptSubmission?.send(submission)
        }
        return HttpResponse.accepted<String>()
    }

    /**
     * Displays the status of a worklog
     * @param taskId the task ID
     * @param worklogId the worklog ID
     * @return the worklog, in the requested format
     */
    @Get("/{taskId}/{worklogId}")
    fun display(@PathVariable taskId : String, @PathVariable worklogId : String, @Header accept : String) : HttpResponse<String> {
        val file = TalFS.getWorklogFile(taskId,worklogId)
        val parsedAccept = if(accept == null) CONTENT_TYPE_APPLICATION_JSON else accept.toLowerCase()
        when(parsedAccept) {
            CONTENT_TYPE_X_YAML ->
                return HttpResponse.ok(file.readText()).contentType(CONTENT_TYPE_X_YAML)
            CONTENT_TYPE_TEXT_YAML ->
                return HttpResponse.ok(file.readText()).contentType(CONTENT_TYPE_TEXT_YAML)
            else -> {
                return  HttpResponse.ok(TalFS.yamlFileToJSON(file)).contentType(CONTENT_TYPE_APPLICATION_JSON)
            }
        }
    }
}