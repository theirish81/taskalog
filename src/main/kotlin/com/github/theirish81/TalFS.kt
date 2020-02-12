package com.github.theirish81

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.theirish81.messages.TalTask
import com.github.theirish81.messages.TalTimer
import java.io.File
import java.util.*

/**
 * FS Operations
 */
object TalFS {

    private val yamlObjectMapper : ObjectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

    private val jsonObjectMapper : ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    fun getWorklogsFile() : File {
        val file = File("worklogs")
        if(!file.exists())
            file.mkdir()
        return file
    }
    fun getTaskWorklogsFile() : File {
        val file = getWorklogsFile().resolve("tasks")
        if(!file.exists())
            file.mkdirs()
        return file
    }

    fun getTaskWorklogCategoryFile(task : String, create : Boolean = true) : File {
        val file = getTaskWorklogsFile().resolve(task)
        if(create && !file.exists())
            file.mkdirs()
        return file
    }

    fun getTaskWorklogFile(task : String, worklogId : String) : File =
            getTaskWorklogCategoryFile(task).resolve(worklogId+".yml")


    fun getEtcFile() : File = File("etc")

    fun getTasksFile() : File = getEtcFile().resolve("tasks")

    fun getTaskFile(taskId : String) : File = getTasksFile().resolve(taskId+".yml")

    fun hasWorklogExpired(taskId : String, worklogId : String) : Boolean =
            getTaskWorklogCategoryFile(taskId).resolve("_"+worklogId).exists()

    fun parseYamlFile(file : File) : Map<*,*> {
        return yamlObjectMapper.readValue(file,Map::class.java)
    }

    fun yamlFileToJSON(file : File) : String =
                        jsonObjectMapper.writeValueAsString(parseYamlFile(file))

    fun <T> deserializeYaml(yaml : File, theClass : Class<T>) : T = yamlObjectMapper.readValue(yaml, theClass)

    fun serializeAsYaml(item : Any) : String = yamlObjectMapper.writeValueAsString(item)

    fun serializeAsJSON(item : Any) : String = jsonObjectMapper.writeValueAsString(item)


    fun loadTask(name : String) : Optional<TalTask> {
        val found = getTaskFile(name)
        if(found.exists())
            return Optional.of(deserializeYaml(found, TalTask::class.java))
        return Optional.empty()
    }

    fun createExpiryFile(taskId : String, worklogId : String) {
        val file = getTaskWorklogCategoryFile(taskId).resolve("_"+worklogId)
        file.createNewFile()
        file.deleteOnExit()
    }

    fun getTimersFile() : File = getEtcFile().resolve("timers")

    fun getTimerFile(timerId : String) : File = getTimersFile().resolve(timerId+".yml")

    fun loadTimer(name : String) : Optional<TalTimer> {
        val found = getTimerFile(name)
        if(found.exists())
            return Optional.of(deserializeYaml(found, TalTimer::class.java))
        return Optional.empty()
    }

    fun getTimerWorklogsFile() : File {
        val file = getWorklogsFile().resolve("timers")
        if(!file.exists())
            file.mkdirs()
        return file
    }

    fun getTimerWorklogFile(timerId : String) : File = getTimerWorklogsFile().resolve(timerId+".yml")

    fun loadTimerWorklog(timerId : String) : Optional<TalTimer> {
        val found = getTimerWorklogFile(timerId)
        if(found.exists())
            return Optional.of(deserializeYaml(found, TalTimer::class.java))
        return Optional.empty()
    }

}