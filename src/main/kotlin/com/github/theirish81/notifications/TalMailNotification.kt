package com.github.theirish81.notifications

import com.github.theirish81.TalFS
import com.github.theirish81.messages.TalStatusAndWorklog
import com.github.theirish81.messages.TalTimer
import com.sun.mail.smtp.SMTPTransport
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.slf4j.LoggerFactory
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.File
import java.util.*
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


object TalMailNotification : ITalNotification {

    val pool = newSingleThreadContext("emailNotificationActorPool")

    val log = LoggerFactory.getLogger(TalMailNotification::class.java)

    val templateEngine = TemplateEngine()

    override fun initialize() {
        log.info("Initializing")
        val resolver = FileTemplateResolver()
        resolver.setTemplateMode("HTML")
        templateEngine.setTemplateResolver(resolver)
    }

    private fun setupProps(config: TalMailNotificationConfig) : Properties {
        val props = Properties()
        props["mail.smtp.timeout"] = "10000"
        props["mail.smtp.connectiontimeout"] = "10000"
        props["mail.smtps.timeout"] = "10000"
        props["mail.smtps.connectiontimeout"] = "10000"

        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.host"] = config.host
        props["mail.smtp.port"] = config.port.toString()
        props["mail.smtp.user"] = config.username
        props["mail.smtp.password"] = config.password
        props["mail.smtp.starttls.enable"] = config.startTLS.toString()
        return props
    }

    private fun sendEmail(text : String, subject : String, config : TalMailNotificationConfig) {
        val props = setupProps(config)
        val session: Session = Session.getInstance(props, null)
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(config.from))
        message.setSubject(subject)
        message.setRecipients(MimeMessage.RecipientType.TO, config.recipient)
        message.setContent(text, "text/html")
        val tr: SMTPTransport = session.getTransport() as SMTPTransport
        tr.connect(config.host, config.port, config.username, config.password)
        message.saveChanges()
        tr.sendMessage(message, message.getAllRecipients())
        tr.close()
    }

    override fun notify(msg : TalStatusAndWorklog) {

        GlobalScope.launch(pool) {
            val config = loadConfig()
            if((config.enabled as Boolean)) {
                val context = Context()
                context.setVariable("msg", msg)
                val template = templateEngine.process(TalFS.getEtcFile().resolve("email_templates")
                        .resolve("task.html").absolutePath, context)
                sendEmail(template, "Failure to comply to task `${msg.worklog.taskId}`", config)
            }
        }
    }

    override fun notify(msg: TalTimer) {
        GlobalScope.launch(pool) {
            val config = loadConfig()
            if((config.enabled as Boolean)) {
                val context = Context()
                context.setVariable("msg", msg)
                val template = templateEngine.process(TalFS.getEtcFile().resolve("email_templates")
                                .resolve("timer.html").absolutePath, context)
                sendEmail(template, "Failure to comply to timer `${msg.id}`", config)
            }
        }
    }

    override fun shutdown() {
        log.info("Shutting down")
    }

    private fun loadConfig() : TalMailNotificationConfig =
            loadConfig(TalFS.getEtcFile().resolve("mail_notification.yml"))

    private fun loadConfig(file : File) : TalMailNotificationConfig =
            TalFS.deserializeYaml(file, TalMailNotificationConfig::class.java)

    data class TalMailNotificationConfig(val from : String, val host : String, val port : Int, val username : String,
                                            val password : String, val startTLS: Boolean, val recipient : String, val enabled: Boolean)
}