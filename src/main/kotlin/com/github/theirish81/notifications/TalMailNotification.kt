package com.github.theirish81.notifications

import com.github.theirish81.TalFS
import com.github.theirish81.messages.TalStatusAndWorklog
import com.github.theirish81.messages.TalTimer
import com.sun.mail.smtp.SMTPTransport
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.util.*
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


object TalMailNotification : ITalNotification {

    val templateEngine = TemplateEngine()
    init {
        val resolver = FileTemplateResolver()
        resolver.setTemplateMode("HTML")
        templateEngine.setTemplateResolver(resolver)
    }

    private fun setupProps(config : Map<*,*>) : Properties {
        val props = Properties()
        props["mail.smtp.timeout"] = "10000"
        props["mail.smtp.connectiontimeout"] = "10000"
        props["mail.smtps.timeout"] = "10000"
        props["mail.smtps.connectiontimeout"] = "10000"

        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.host"] = config["host"]
        props["mail.smtp.port"] = config["port"].toString()
        props["mail.smtp.user"] = config["username"].toString()
        props["mail.smtp.password"] = config["password"].toString()
        props["mail.smtp.starttls.enable"] = config["startTLS"].toString()
        return props
    }

    private fun sendEmail(text : String, subject : String) {
        val config = TalFS.parseYamlFile(TalFS.getEtcFile().resolve("mail_notification.yml"))
        if(!(config["enabled"] as Boolean))
            return
        val props = setupProps(config)
        val session: Session = Session.getInstance(props, null)
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(config["from"].toString()))
        message.setSubject(subject)
        message.setRecipients(MimeMessage.RecipientType.TO, config["recipient"].toString())
        message.setContent(text, "text/html")
        val tr: SMTPTransport = session.getTransport() as SMTPTransport
        tr.connect(config["host"].toString(), config["port"] as Int, config["username"].toString(), config["password"].toString())
        message.saveChanges()
        tr.sendMessage(message, message.getAllRecipients())
        tr.close()
    }

    override fun notify(msg : TalStatusAndWorklog) {

        GlobalScope.launch {
            val context = Context()
            context.setVariable("msg",msg)
            val template = templateEngine.process(TalFS.getEtcFile().resolve("etc_templates")
                                                        .resolve("task.html").absolutePath,context)
            sendEmail(template, "Failure to comply to task `${msg.worklog.taskId}`")
        }
    }

    override fun notify(msg: TalTimer) {
        GlobalScope.launch {
            val context = Context()
            context.setVariable("msg",msg)
            val template = templateEngine.process("etc/email_templates/timer.html",context)
            sendEmail(template, "Failure to comply to timer `${msg.id}`")
        }
    }
}