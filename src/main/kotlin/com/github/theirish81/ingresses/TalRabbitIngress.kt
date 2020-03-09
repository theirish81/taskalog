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
package com.github.theirish81.ingresses

import com.github.theirish81.TalFS
import com.github.theirish81.actors.TalTaskActors
import com.github.theirish81.actors.TalTimerActors
import com.github.theirish81.messages.TalSubmission
import com.github.theirish81.messages.TalTimer
import com.rabbitmq.client.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * The RabbitMQ ingress
 */
object TalRabbitIngress : ITalIngress {

    /**
     * A connection to the RabbitMQ ingress
     */
    var connection : Connection? = null
    /**
     * A channel to listen to tasks submissions
     */
    var tasksChannel : Channel? = null
    /**
     * A channel to listen to timer submissions
     */
    var timersChannel : Channel? = null

    /**
     * The logger
     */
    val log = LoggerFactory.getLogger(TalRabbitIngress::class.java)

    override fun start() {
        log.info("Starting")
        val configFile = TalFS.getEtcFile().resolve("rabbitmq_ingress.yml")
        if(configFile.exists()) {
            val data : Map<String,Any> = TalFS.deserializeYaml(configFile,Map::class.java) as Map<String,Any>
            if(data["enabled"] as Boolean){
                val uri = data["uri"] as String
                val factory = ConnectionFactory()
                factory.setUri(uri)
                connection = factory.newConnection()
                tasksChannel = connection!!.createChannel()
                timersChannel = connection!!.createChannel()
                val tasksConsumer = TasksConsumer(tasksChannel!!)
                val timersConsumer = TimersConsumer(timersChannel!!)
                tasksChannel!!.basicConsume("taskalog_tasks",true, tasksConsumer)
                timersChannel!!.basicConsume("taskalog_timers", true, timersConsumer)
            }
        }
    }

    /**
     * The RabbitMQ consumer for tasks submissions
     * @param channel the channel
     */
    class TasksConsumer(channel : Channel) : DefaultConsumer(channel) {
        override fun handleDelivery(consumerTag: String?, envelope: Envelope?, properties: AMQP.BasicProperties?, body: ByteArray?) {
            val talSubmission = TalFS.deserializeJSON(body!!,TalSubmission::class.java)
            GlobalScope.launch {
                TalTaskActors.acceptSubmission!!.send(talSubmission)
            }

        }
    }

    /**
     * The RabbitMQ consumer for timer submissions
     * @param channel the channel
     */
    class TimersConsumer(channel : Channel) : DefaultConsumer(channel) {
        override fun handleDelivery(consumerTag: String?, envelope: Envelope?, properties: AMQP.BasicProperties?, body: ByteArray?) {
            val talTimer = TalFS.deserializeJSON(body!!,TalTimer::class.java)
            GlobalScope.launch {
                TalTimerActors.acceptSubmission!!.send(talTimer)
            }
        }
    }

    override fun shutdown() {
        log.info("Shutting down")
        tasksChannel!!.close()
        timersChannel!!.close()
        connection!!.close()
    }
}