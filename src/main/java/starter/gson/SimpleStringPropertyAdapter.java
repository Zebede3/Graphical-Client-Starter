package starter.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.beans.property.SimpleStringProperty;

public class SimpleStringPropertyAdapter implements JsonSerializer<SimpleStringProperty>, JsonDeserializer<SimpleStringProperty> {

	@Override
	public SimpleStringProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		final JsonElement obj = json.getAsJsonObject().get("value");
		if (obj == null)
			return new SimpleStringProperty();
		return new SimpleStringProperty(obj.getAsString());
	}

	@Override
	public JsonElement serialize(SimpleStringProperty src, Type typeOfSrc, JsonSerializationContext context) {
		final JsonObject member = new JsonObject();
		member.addProperty("value", src.get());
		return member;
	}
	
}
