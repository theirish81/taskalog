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
package com.github.theirish81.taskalog.notifications

import com.github.theirish81.taskalog.TalFS
import com.github.theirish81.taskalog.messages.TalStatusAndWorklog
import com.github.theirish81.taskalog.messages.TalTimer
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.RxHttpClient
import kotlinx.coroutines.newSingleThreadContext
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.util.*

/**
 * Notifications via Web Hooks
 */
object TalHookNotification : ITalNotification {

    /**
     * Micronaut application context
     */
    private val applicationContext = ApplicationContext.run()

    /**
     * RX HTTP Client
     */
    private val client = applicationContext.createBean(RxHttpClient::class.java,"http://localhost")

    val log = LoggerFactory.getLogger(TalHookNotification::class.java)

    override fun initialize() {}

    override fun notify(msg: TalStatusAndWorklog) {
        val config = loadConfig()
        if(config.enabled)
            config.tasks.forEach {
                val request = HttpRequest.POST(it.url,TalFS.serializeAsJSON(msg))
                        .contentType(MediaType.APPLICATION_JSON_TYPE)
                it.headers.forEach { (t, u) ->
                    request.header(t,u)
                }
                client.exchange(request,String::class.java).subscribe{ response ->
                    if(response.status.code<400)
                        log.debug("Hook request for ${it.url} completed")
                    else
                        log.error("Hook request for ${it.url} failed with error ${response.status.code}")
                }
            }

    }

    override fun notify(msg: TalTimer) {
        val config = loadConfig()
        if(config.enabled)
            config.timers.forEach {
                val request = HttpRequest.POST(it.url,TalFS.serializeAsJSON(msg))
                                    .contentType(MediaType.APPLICATION_JSON_TYPE)
                it.headers.forEach { (t, u) ->
                    request.header(t,u)
                }

                client.exchange(request,String::class.java).subscribe{ response ->
                    if(response.status.code<400)
                        log.debug("Hook request for ${it.url} completed")
                    else
                        log.error("Hook request for ${it.url} failed with error ${response.status.code}")
                }
            }
    }

    override fun shutdown() {
        client.close()
    }

    /**
     * Loads the default configuration file
     */
    private fun loadConfig() : TalHookNotificationConfig {
        return loadConfig(TalFS.getEtcFile().resolve("hook_notification.yml"))
    }

    /**
     * Loads a Hook Notification configuration file
     * @param file the configuration file
     */
    private fun loadConfig(file : File) : TalHookNotificationConfig {
        return TalFS.deserializeYaml(file,TalHookNotificationConfig::class.java)
    }

    /**
     * The configuration of one Web Hook endpoint
     * @param url URL of the Web Hook
     * @param headers the headers to be part of the Web Hook requests
     */
    data class TalHookEntryConfig(val url : String, val headers : Map<String,String>)

    /**
     * A block of Web Hook configurations
     */
    class TalHookConfigurationBlock : LinkedList<TalHookEntryConfig>()

    /**
     * The overall configuration
     * @param tasks the Web Hook configurations for the tasks
     * @param timers the Web Hook configurations for the timers
     * @param enabled set to true to enable this mode of configuration
     */
    data class TalHookNotificationConfig(val tasks : TalHookConfigurationBlock,
                                         val timers : TalHookConfigurationBlock,
                                         val enabled : Boolean)
}