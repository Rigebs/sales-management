package com.rige.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import java.math.BigDecimal

object BigDecimalSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by JSON")

        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonPrimitive) {
            throw SerializationException("Expected JsonPrimitive but got ${element::class.simpleName}")
        }

        return try {
            BigDecimal(element.content)
        } catch (e: NumberFormatException) {
            throw SerializationException("Invalid BigDecimal value: ${element.content}", e)
        }
    }
}