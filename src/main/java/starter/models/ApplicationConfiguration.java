package starter.models;

import java.util.Objects;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

/**
 * This information persists across save files
 *
 */
public class ApplicationConfiguration {

	private final SimpleBooleanProperty dontShowExitConfirm = new SimpleBooleanProperty(false);

	private final SimpleBooleanProperty dontShowSaveConfirm = new SimpleBooleanProperty(false);
	
	private final SimpleBooleanProperty autoSaveLast = new SimpleBooleanProperty(true);
	
	private final SimpleObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.DEFAULT);
	
	private final SimpleDoubleProperty widthProperty = new SimpleDoubleProperty(Double.NaN);
	private final SimpleDoubleProperty heightProperty = new SimpleDoubleProperty(Double.NaN);
	
	private final SimpleDoubleProperty xProperty = new SimpleDoubleProperty(Double.NaN);
	private final SimpleDoubleProperty yProperty = new SimpleDoubleProperty(Double.NaN);
	
	public boolean isAutoSaveLast() {
		return this.autoSaveLast.get();
	}
	
	public void setAutoSaveLast(boolean autoSave) {
		this.autoSaveLast.set(autoSave);
	}
	
	public boolean isDontShowExitConfirm() {
		return this.dontShowExitConfirm.get();
	}
	
	public void setDontShowExitConfirm(boolean dontShow) {
		this.dontShowExitConfirm.set(dontShow);
	}
	
	public boolean isDontShowSaveConfirm() {
		return this.dontShowSaveConfirm.get();
	}
	
	public void setDontShowSaveConfirm(boolean dontShow) {
		this.dontShowSaveConfirm.set(dontShow);
	}
	
	public Theme getTheme() {
		// backwards compatibility stuff 10/15
		final Theme theme = this.theme.get();
		if (theme == null) {
			this.theme.set(Theme.DEFAULT);
			return Theme.DEFAULT;
		}
		return theme;
	}
	
	public void setTheme(Theme theme) {
		this.theme.set(Objects.requireNonNull(theme));
	}
	
	public SimpleObjectProperty<Theme> themeProperty() {
		return this.theme;
	}
	
	public double getWidth() {
		return this.widthProperty.get();
	}
	
	public void setWidth(double width) {
		this.widthProperty.set(width);
	}
	
	public SimpleDoubleProperty widthProperty() {
		return this.widthProperty;
	}
	
	public double getHeight() {
		return this.heightProperty.get();
	}
	
	public void setHeight(double height) {
		this.heightProperty.set(height);
	}
	
	public SimpleDoubleProperty heightProperty() {
		return this.heightProperty;
	}

	public double getX() {
		return this.xProperty.get();
	}
	
	public void setX(double x) {
		this.xProperty.set(x);
	}
	
	public SimpleDoubleProperty xProperty() {
		return this.xProperty;
	}
	
	public double getY() {
		return this.yProperty.get();
	}
	
	public void setY(double y) {
		this.yProperty.set(y);
	}
	
	public SimpleDoubleProperty yProperty() {
		return this.yProperty;
	}
	
	public void runOnChange(Runnable run) {
		addListeners(run, this.dontShowExitConfirm, this.dontShowSaveConfirm, this.autoSaveLast,
				this.theme, this.widthProperty, this.heightProperty, this.xProperty, this.yProperty);
	}

	public SimpleBooleanProperty autoSaveLastProperty() {
		return this.autoSaveLast;
	}
	
	private void addListeners(Runnable run, ObservableValue<?>... obs) {
		for (ObservableValue<?> ob : obs)
			ob.addListener((o, old, newv) -> run.run());
	}
	
}
