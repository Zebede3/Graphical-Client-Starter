package starter.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// The type within the list must be json serializable
public class ObservableListAdapter implements JsonSerializer<ObservableList<?>>, JsonDeserializer<ObservableList<?>> {

	private final Type elementType;
	
	public ObservableListAdapter(Type elementType) {
		this.elementType = elementType;
	}
	
	@Override
	public ObservableList<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		final JsonElement element = json.getAsJsonObject().get("list");
		if (element == null)
			return FXCollections.observableArrayList();
		return FXCollections.observableArrayList(
				context.<ArrayList<?>>deserialize(element,
				TypeToken.getParameterized(ArrayList.class, this.elementType).getType()));
	}

	@Override
	public JsonElement serialize(ObservableList<?> src, Type typeOfSrc, JsonSerializationContext context) {
		final JsonObject member = new JsonObject();
		member.add("list", context.serialize(new ArrayList<>(src),
							TypeToken.getParameterized(ArrayList.class, this.elementType).getType()));
		return member;
	}
	
}
