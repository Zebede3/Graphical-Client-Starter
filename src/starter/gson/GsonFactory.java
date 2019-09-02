package starter.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import starter.models.AccountConfiguration;
import starter.models.ProxyDescriptor;

public class GsonFactory {

	public static Gson buildGson() {
		return new GsonBuilder()
				.registerTypeAdapter(SimpleBooleanProperty.class, new SimpleBooleanPropertyAdapter())
				.registerTypeAdapter(SimpleStringProperty.class, new SimpleStringPropertyAdapter())
				.registerTypeAdapter(TypeToken.getParameterized(ObservableList.class, AccountConfiguration.class).getType(),
						new ObservableListAdapter(AccountConfiguration.class))
				.registerTypeAdapter(SimpleIntegerProperty.class, new SimpleIntegerPropertyAdapter())
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, ProxyDescriptor.class).getType(),
						new SimpleObjectPropertyAdapter(ProxyDescriptor.class))
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, Color.class).getType(),
						new SimpleObjectPropertyAdapter(Color.class))
				.create();
	}
	
}
