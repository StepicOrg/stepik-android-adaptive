package org.stepik.android.adaptive.util.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import org.stepik.android.adaptive.data.model.Dataset
import org.stepik.android.adaptive.data.model.DatasetWrapper
import java.lang.reflect.Type

class DatasetWrapperDeserializer : JsonDeserializer<DatasetWrapper> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DatasetWrapper =
        if (json !is JsonObject) {
            try {
                val o = context.deserialize<Any>(json, String::class.java)
                val dataset = Dataset(o as String)
                DatasetWrapper(dataset)
            } catch (e: Exception) {
                // if it is primitive, but not string.
                DatasetWrapper()
            }
        } else {
            DatasetWrapper(context.deserialize<Any>(json, Dataset::class.java) as Dataset)
        }
}
