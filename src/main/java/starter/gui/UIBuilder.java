package starter.gui;

import java.io.IOException;
import java.util.function.BiConsumer;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import starter.models.ApplicationConfiguration;
import starter.models.Theme;

/**
 * This can only be used after the initial javafx application has been launched
 * Used for child windows
 *
 */
public class UIBuilder {

	private String windowName;
	private String fxml;
	private BiConsumer<Stage, ?> onCreation;
	private Stage parent;
	private ApplicationConfiguration config;
	
	public UIBuilder withApplicationConfig(ApplicationConfiguration config) {
		this.config = config;
		return this;
	}

	public UIBuilder withWindowName(String name) {
		this.windowName = name;
		return this;
	}
	
	public UIBuilder withFxml(String name) {
		this.fxml = name;
		return this;
	}
	
	public <T> UIBuilder onCreation(BiConsumer<Stage, T> stage) {
		this.onCreation = stage;
		return this;
	}
	
	public UIBuilder withParent(Stage parent) {
		this.parent = parent;
		return this;
	}
	
	public void build() {
		if (this.fxml == null)
			throw new NullPointerException("fxml must not be null");
		if (this.config == null)
			throw new NullPointerException("config must not be null");
		if (this.parent == null)
			throw new NullPointerException("parent must not be null");
		if (this.windowName == null)
			this.windowName = "";
		if (this.onCreation == null)
			this.onCreation = (stage, controller) -> {};
		final Stage stage = new Stage();
		stage.initOwner(this.parent);
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			final Parent root = (Parent) loader.load();
			stage.setTitle(this.windowName);
			final Scene scene = new Scene(root);
			stage.setScene(scene);
			final ChangeListener<Theme> themeListener = (obs, old, newv) -> {
				stage.getScene().getStylesheets().setAll(newv.getCss());
			};
			themeListener.changed(this.config.themeProperty(), null, this.config.getTheme());
			this.config.themeProperty().addListener(themeListener);
			stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, e -> this.config.themeProperty().removeListener(themeListener));
			this.onCreation.accept(stage, loader.getController());
			stage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
