package com.github.theirish81

import com.github.theirish81.actors.TalTaskActors
import com.github.theirish81.messages.TalWorklog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


object TalTickers {

    val taskTimer = Timer()

    fun init() {
        taskTimer.scheduleAtFixedRate(CheckWorklogs(),1000,10000)
    }
}

class CheckWorklogs : TimerTask() {
    override fun run() {
        TalFS.getWorklogsFile().listFiles().filter{ it.isDirectory }.forEach { category ->
            category.listFiles().filter{ it.extension == "yaml" }.forEach { worklog ->
                GlobalScope.launch {
                    TalTaskActors.evaluateWorklog!!.send(TalFS.deserializeYaml(worklog,TalWorklog::class.java))
                }
            }
        }
    }

}