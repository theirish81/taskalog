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

object TalRabbitIngress : ITalIngress {

    var connection : Connection? = null
    var tasksChannel : Channel? = null
    var timersChannel : Channel? = null

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

    class TasksConsumer(channel : Channel) : DefaultConsumer(channel) {
        override fun handleDelivery(consumerTag: String?, envelope: Envelope?, properties: AMQP.BasicProperties?, body: ByteArray?) {
            val talSubmission = TalFS.deserializeJSON(body!!,TalSubmission::class.java)
            GlobalScope.launch {
                TalTaskActors.acceptSubmission!!.send(talSubmission)
            }

        }
    }

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