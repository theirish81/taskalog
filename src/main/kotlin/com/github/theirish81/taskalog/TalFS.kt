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
package com.github.theirish81.taskalog

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.theirish81.taskalog.messages.TalTask
import com.github.theirish81.taskalog.messages.TalTimer
import java.io.File
import java.util.*

/**
 * FS Operations
 */
object TalFS {

    /**
     * The YAML parser
     */
    private val yamlObjectMapper : ObjectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

    /**
     * the JSON parser
     */
    private val jsonObjectMapper : ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    /**
     * @return the worklogs directory
     */
    fun getWorklogsFile() : File {
        val file = File("worklogs")
        if(!file.exists())
            file.mkdir()
        return file
    }

    /**
     * @return the tasks directory
     */
    fun getTaskWorklogsFile() : File {
        val file = getWorklogsFile().resolve("tasks")
        if(!file.exists())
            file.mkdirs()
        return file
    }

    /**
     * @param task the task ID
     * @param create true if the directory should be created if not existent
     * @return the worklog directory for a given task
     */
    fun getTaskWorklogCategoryFile(task : String, create : Boolean = true) : File {
        val file = getTaskWorklogsFile().resolve(task)
        if(create && !file.exists())
            file.mkdirs()
        return file
    }

    /**
     * @param task the task ID
     * @param worklogId the worklog ID
     * @return the worklog file for a given task and ID
     */
    fun getTaskWorklogFile(task : String, worklogId : String) : File =
            getTaskWorklogCategoryFile(task).resolve(worklogId+".yml")


    /**
     * @return the etc directory
     */
    fun getEtcFile() : File = File("etc")

    /**
     * @return the tasks directory
     */
    fun getTasksFile() : File = getEtcFile().resolve("tasks")

    /**
     * @param taskId the task ID
     * @return the file for a certain task
     */
    fun getTaskFile(taskId : String) : File = getTasksFile().resolve(taskId+".yml")

    /**
     * @param taskId the task ID
     * @param worklogId the worklog ID
     * @return check if a worklog expired and left the expiry placeholer file
     */
    fun hasWorklogExpired(taskId : String, worklogId : String) : Boolean =
            getTaskWorklogCategoryFile(taskId).resolve("_"+worklogId).exists()

    /**
     * Parses a YAML file
     * @param file a configuration file
     * @return the parsed file in the form of a map
     */
    fun parseYamlFile(file : File) : Map<*,*> {
        return yamlObjectMapper.readValue(file,Map::class.java)
    }

    /**
     * Converts a YAML file to a JSON string
     * @param file a YAML file
     * @return the file converted to JSON
     */
    fun yamlFileToJSON(file : File) : String =
                        jsonObjectMapper.writeValueAsString(parseYamlFile(file))

    /**
     * Deserializes a YAML file
     * @param yaml a YAML file
     * @param theClass the class to deserialize to
     * @return the deserialized file as an instance of the passed class
     */
    fun <T> deserializeYaml(yaml : File, theClass : Class<T>) : T = yamlObjectMapper.readValue(yaml, theClass)

    /**
     * Deserializes a JSON string
     * @param data a JSON string as a byte array
     * @param theClass the class to deserialize to
     * @return the deserialized string as an instance of the passed class
     */
    fun <T> deserializeJSON(data : ByteArray, theClass : Class<T>) : T = jsonObjectMapper.readValue(data, theClass)

    /**
     * Serializes an object to a YAML string
     * @param item an object
     * @return a YAML string
     */
    fun serializeAsYaml(item : Any) : String = yamlObjectMapper.writeValueAsString(item)

    /**
     * Serializes an object to a JSON string
     * @param item an object
     * @return a JSON string
     */
    fun serializeAsJSON(item : Any) : String = jsonObjectMapper.writeValueAsString(item)


    /**
     * Loads a task
     * @param name a task name
     * @return an Optional[TalTask]
     */
    fun loadTask(name : String) : Optional<TalTask> {
        val found = getTaskFile(name)
        if(found.exists())
            return Optional.of(deserializeYaml(found, TalTask::class.java))
        return Optional.empty()
    }

    /**
     * Creates an expiry file
     * @param taskId a task ID
     * @param worklogId a worklog ID
     */
    fun createExpiryFile(taskId : String, worklogId : String) {
        val file = getTaskWorklogCategoryFile(taskId).resolve("_"+worklogId)
        file.createNewFile()
        file.deleteOnExit()
    }

    /**
     * @return timers directory
     */
    fun getTimersFile() : File = getEtcFile().resolve("timers")

    /**
     * @param timerId a timer ID
     * @return a timer file
     */
    fun getTimerFile(timerId : String) : File = getTimersFile().resolve(timerId+".yml")

    /**
     * Loads a timer
     * @param name a timer name
     * @return an Optional[TalTimer]
     */
    fun loadTimer(name : String) : Optional<TalTimer> {
        val found = getTimerFile(name)
        if(found.exists())
            return Optional.of(deserializeYaml(found, TalTimer::class.java))
        return Optional.empty()
    }

    /**
     * @return the timer worklogs directory
     */
    fun getTimerWorklogsFile() : File {
        val file = getWorklogsFile().resolve("timers")
        if(!file.exists())
            file.mkdirs()
        return file
    }

    /**
     * @param timerId a timer ID
     * @return a timer worklog file
     */
    fun getTimerWorklogFile(timerId : String) : File = getTimerWorklogsFile().resolve(timerId+".yml")

    /**
     * @param timerId a timer ID
     * @return an Optional[TalTimer]
     */
    fun loadTimerWorklog(timerId : String) : Optional<TalTimer> {
        val found = getTimerWorklogFile(timerId)
        if(found.exists())
            return Optional.of(deserializeYaml(found, TalTimer::class.java))
        return Optional.empty()
    }

}