package com.github.theirish81

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.theirish81.messages.TalTask
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

    fun getWorklogCategoryFile(task : String, create : Boolean = true) : File {
        val file = getWorklogsFile().resolve(task)
        if(create && !file.exists())
            file.mkdir()
        return file
    }

    fun getWorklogFile(task : String, worklogId : String) : File =
            getWorklogCategoryFile(task).resolve(worklogId+".yaml")


    fun getEtcFile() : File = File("etc")

    fun getTasksFile() : File = getEtcFile().resolve("tasks")

    fun getTaskFile(taskId : String) : File = getTasksFile().resolve(taskId+".yaml")

    fun hasWorklogExpired(taskId : String, worklogId : String) : Boolean =
            getWorklogCategoryFile(taskId).resolve("_"+worklogId).exists()

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
        val file = getWorklogCategoryFile(taskId).resolve("_"+worklogId)
        file.createNewFile()
        file.deleteOnExit()
    }
}