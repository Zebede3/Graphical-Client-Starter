package starter.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.beans.property.SimpleDoubleProperty;

public class SimpleDoublePropertyAdapter implements JsonSerializer<SimpleDoubleProperty>, JsonDeserializer<SimpleDoubleProperty> {

	@Override
	public SimpleDoubleProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return new SimpleDoubleProperty(json.getAsJsonObject().get("value").getAsDouble());
	}

	@Override
	public JsonElement serialize(SimpleDoubleProperty src, Type typeOfSrc, JsonSerializationContext context) {
		final JsonObject member = new JsonObject();
		member.addProperty("value", src.get());
		return member;
	}
	
}
