package starter.gson;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import starter.models.AccountConfiguration;
import starter.models.ProxyDescriptor;
import starter.models.SelectionMode;
import starter.models.Theme;
import starter.util.AccountImportParser.AccountImportField;

public class GsonFactory {
	
	private static Gson prettyInstance;
	private static Gson plainInstance;
	
	public static Gson buildGson() {
		return buildGson(true);
	}
	
	public static Gson buildGson(boolean pretty) {
		if (pretty) {
			if (prettyInstance == null) {
				synchronized (GsonFactory.class) {
					if (prettyInstance == null) { // double check after synchronizing
						prettyInstance = build(pretty);
					}
				}
			}
			return prettyInstance;
		}
		else {
			if (plainInstance == null) {
				synchronized (GsonFactory.class) {
					if (plainInstance == null) { // double check after synchronizing
						plainInstance = build(pretty);
					}
				}
			}
			return plainInstance;
		}
	}
	
	private static Gson build(boolean pretty) {
		final GsonBuilder builder = new GsonBuilder()
				.enableComplexMapKeySerialization()
				.serializeSpecialFloatingPointValues()
				.registerTypeAdapter(SimpleBooleanProperty.class, new SimpleBooleanPropertyAdapter())
				.registerTypeAdapter(SimpleStringProperty.class, new SimpleStringPropertyAdapter())
				.registerTypeAdapter(SimpleIntegerProperty.class, new SimpleIntegerPropertyAdapter())
				.registerTypeAdapter(SimpleDoubleProperty.class, new SimpleDoublePropertyAdapter())
				.registerTypeAdapter(TypeToken.getParameterized(ObservableList.class, AccountConfiguration.class).getType(),
						new ObservableListAdapter(AccountConfiguration.class))
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, ProxyDescriptor.class).getType(),
						new SimpleObjectPropertyAdapter(ProxyDescriptor.class))
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, Color.class).getType(),
						new SimpleObjectPropertyAdapter(Color.class))
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, Theme.class).getType(),
						new SimpleObjectPropertyAdapter(Theme.class))
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, SelectionMode.class).getType(),
						new SimpleObjectPropertyAdapter(SelectionMode.class))
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, LocalTime.class).getType(),
						new SimpleObjectPropertyAdapter(LocalTime.class))
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, LocalDate.class).getType(),
						new SimpleObjectPropertyAdapter(LocalDate.class))
				.registerTypeAdapter(TypeToken.getParameterized(ObservableList.class, ProxyDescriptor.class).getType(),
						new ObservableListAdapter(ProxyDescriptor.class))
				.registerTypeAdapter(TypeToken.getParameterized(ObservableList.class, Integer.class).getType(),
						new ObservableListAdapter(Integer.class))
				.registerTypeAdapter(TypeToken.getParameterized(SimpleObjectProperty.class, TypeToken.getParameterized(Map.class, AccountImportField.class, String.class).getType()).getType(),
						new SimpleObjectPropertyAdapter(TypeToken.getParameterized(Map.class, AccountImportField.class, String.class).getType()));
		if (pretty) {
			builder.setPrettyPrinting();
		}
		return builder.create();
	}
	
}
