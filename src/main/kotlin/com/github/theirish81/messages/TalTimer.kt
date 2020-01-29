package com.github.theirish81.messages

import com.fasterxml.jackson.annotation.JsonProperty

data class TalTimer(val id : String, @JsonProperty("every_seconds") val everySeconds : Int,
                    @JsonProperty("message_regex")val messageRegex : List<String>)