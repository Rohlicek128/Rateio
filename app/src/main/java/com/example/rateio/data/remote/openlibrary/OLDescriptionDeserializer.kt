package com.example.rateio.data.remote.openlibrary

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type


class OLTypeValueDeserializer : JsonDeserializer<OLTypeValue> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: com.google.gson.JsonDeserializationContext
    ): OLTypeValue? {
        return when {
            json.isJsonPrimitive -> {
                OLTypeValue(type = "/type/text", value = json.asString)
            }
            json.isJsonObject -> {
                val jsonObject = json.asJsonObject
                val extractedValue = if (jsonObject.has("value")) {
                    jsonObject.get("value").asString
                } else {
                    ""
                }
                val extractedType = if (jsonObject.has("type")) {
                    jsonObject.get("type").asString
                } else {
                    ""
                }
                OLTypeValue(type = extractedType, value = extractedValue)
            }
            else -> null
        }
    }
}