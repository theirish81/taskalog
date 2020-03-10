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
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.slf4j.LoggerFactory
import java.io.File

/**
 * The RabbitMQ notification implementation
 */
object TalRabbitNotification : ITalNotification {

    /**
     * The connection to the RabbitMQ server
     */
    var connection : Connection? = null
    /**
     * The channel to emit tasks notifications
     */
    var tasksChannel : Channel? = null
    /**
     * The channel to emit timers notifications
     */
    var timersChannel : Channel? = null

    /**
     * The logger
     */
    val log = LoggerFactory.getLogger(TalRabbitNotification::class.java)

    override fun initialize() {
        val configFile = TalFS.getEtcFile().resolve("rabbitmq_notification.yml")
        if(configFile.exists()) {
            val config = loadConfig(configFile)
            if(config.enabled) {
                log.info("Initializing")
                val uri = config.uri
                val factory = ConnectionFactory()
                factory.setUri(uri)
                connection = factory.newConnection()
                tasksChannel = connection!!.createChannel()
                timersChannel = connection!!.createChannel()
            }
        }
    }
    override fun notify(msg: TalStatusAndWorklog) {
        log.debug("Publishing TalStatusAndWorklog to RabbitMQ")
        if(loadConfig().enabled)
            tasksChannel!!.basicPublish("taskalog_notifications",
                            "tasks",AMQP.BasicProperties(),
                                        TalFS.serializeAsJSON(msg).toByteArray(Charsets.UTF_8))
        else
            log.debug("Notificator disabled")
    }

    override fun notify(msg: TalTimer) {
        log.debug("Publishing TalTimer to RabbitMQ")
        if(loadConfig().enabled)
            timersChannel!!.basicPublish("taskalog_notifications",
                    "timers",AMQP.BasicProperties(),
                    TalFS.serializeAsJSON(msg).toByteArray(Charsets.UTF_8))
        else
            log.debug("Notificator disabled")
    }

    /**
     * Loads the RabbitMQ notification config
     * @return the RabbitMQ notification config
     */
    private fun loadConfig() : TalRabbitNotificationConfig =
        loadConfig(TalFS.getEtcFile().resolve("rabbitmq_notification.yml"))

    /**
     * Loads the RabbitMQ notification config
     * @param file the file containing the configuration
     * @return the RabbitMQ notification config
     */
    private fun loadConfig(file : File) : TalRabbitNotificationConfig =
        TalFS.deserializeYaml(file,TalRabbitNotificationConfig::class.java)

    override fun shutdown() {
        log.info("Shutting down")
        tasksChannel!!.close()
        timersChannel!!.close()
        connection!!.close()
    }

    /**
     * The bean containing the RabbitMQ notification config
     * @param uri the RabbitMQ URI
     * @param enabled true if the notification system is enabled
     */
    data class TalRabbitNotificationConfig(val uri : String, val enabled : Boolean)
}