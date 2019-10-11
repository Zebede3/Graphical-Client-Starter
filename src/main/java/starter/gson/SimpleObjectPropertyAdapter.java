package starter.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javafx.beans.property.SimpleObjectProperty;

public class SimpleObjectPropertyAdapter implements JsonSerializer<SimpleObjectProperty<?>>, JsonDeserializer<SimpleObjectProperty<?>> {

	private final Type type;
	
	public SimpleObjectPropertyAdapter(Type type) {
		this.type = type;
	}
	
	@Override
	public SimpleObjectProperty<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return new SimpleObjectProperty<>(context.deserialize(json.getAsJsonObject().get("obj"), this.type));
	}

	@Override
	public JsonElement serialize(SimpleObjectProperty<?> src, Type typeOfSrc, JsonSerializationContext context) {
		final JsonObject member = new JsonObject();
		member.add("obj", context.serialize(src.get(), this.type));
		return member;
	}
	
}
