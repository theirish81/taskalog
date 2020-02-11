package com.github.theirish81.notifications

import com.github.theirish81.TalFS
import com.github.theirish81.messages.TalStatusAndWorklog
import com.sun.mail.smtp.SMTPTransport
import java.util.*
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class TalMailNotification : ITalNotification {
    override fun notify(msg : TalStatusAndWorklog) {
        val template = """Failure to comply for task `${msg.worklog.taskId}` in worklog `${msg.worklog.id}
            |date : ${msg.status.evaluationDate}
            |Valid: ${msg.status.valid} - Details: ${msg.status.details}
            |late: ${msg.status.late}
        """.trimMargin()
        val config = TalFS.parseYamlFile(TalFS.getEtcFile().resolve("mail_notification.yml"))
        val props = Properties()

        // Introducing timeouts. Who would have thought...
        // Introducing timeouts. Who would have thought...
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

        val session: Session = Session.getInstance(props, null)
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(config["from"].toString()))
        message.setSubject("Failure to comply to task `${msg.worklog.taskId}`")
        message.setRecipients(MimeMessage.RecipientType.TO, config["recipient"].toString())
        message.setContent(template, "text/plain")
        val tr: SMTPTransport = session.getTransport() as SMTPTransport
        tr.connect(config["host"].toString(), config["port"] as Int, config["username"].toString(), config["password"].toString())
        message.saveChanges()
        tr.sendMessage(message, message.getAllRecipients())
        tr.close()

    }
}