package com.github.theirish81.notifications

import com.github.theirish81.TalFS
import com.github.theirish81.messages.TalStatusAndWorklog
import com.github.theirish81.messages.TalTimer
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.slf4j.LoggerFactory
import java.io.File

object TalRabbitNotification : ITalNotification {

    var connection : Connection? = null
    var tasksChannel : Channel? = null
    var timersChannel : Channel? = null

    val log = LoggerFactory.getLogger(TalRabbitNotification::class.java)

    override fun initialize() {
        val configFile = TalFS.getEtcFile().resolve("rabbitmq_notification.yml")
        if(configFile.exists()) {
            log.info("Initializing")
            val uri = loadConfig(configFile).uri
            val factory = ConnectionFactory()
            factory.setUri(uri)
            connection = factory.newConnection()
            tasksChannel = connection!!.createChannel()
            timersChannel = connection!!.createChannel()
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

    private fun loadConfig() : TalRabbitNotificationConfig =
        loadConfig(TalFS.getEtcFile().resolve("rabbitmq_notification.yml"))

    private fun loadConfig(file : File) : TalRabbitNotificationConfig =
        TalFS.deserializeYaml(file,TalRabbitNotificationConfig::class.java)

    override fun shutdown() {
        log.info("Shutting down")
        tasksChannel!!.close()
        timersChannel!!.close()
        connection!!.close()
    }

    data class TalRabbitNotificationConfig(val uri : String, val enabled : Boolean)
}