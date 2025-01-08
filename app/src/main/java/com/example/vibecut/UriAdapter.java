package com.example.vibecut;

import android.net.Uri;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class UriAdapter implements JsonSerializer<Uri>, JsonDeserializer<Uri> {

    @Override
    public JsonElement serialize(Uri uri, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(uri.toString());
    }

    @Override
    public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Uri.parse(json.getAsString());
    }
}