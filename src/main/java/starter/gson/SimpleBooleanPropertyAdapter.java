package starter.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.beans.property.SimpleBooleanProperty;

public class SimpleBooleanPropertyAdapter implements JsonSerializer<SimpleBooleanProperty>, JsonDeserializer<SimpleBooleanProperty> {

	@Override
	public SimpleBooleanProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return new SimpleBooleanProperty(json.getAsJsonObject().get("enabled").getAsBoolean());
	}

	@Override
	public JsonElement serialize(SimpleBooleanProperty src, Type typeOfSrc, JsonSerializationContext context) {
		final JsonObject member = new JsonObject();
		member.addProperty("enabled", src.get());
		return member;
	}
	
}
