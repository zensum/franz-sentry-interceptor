package se.zensum

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val jacksonObjectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
fun <T> T.toJson() = jacksonObjectMapper.writeValueAsString(this)
inline fun <reified T> String.fromJson() = jacksonObjectMapper.readValue(this, T::class.java)