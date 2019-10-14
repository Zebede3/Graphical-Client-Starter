package starter.models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

/**
 * This information persists across save files
 *
 */
public class ApplicationConfiguration {

	private final SimpleBooleanProperty dontShowExitConfirm = new SimpleBooleanProperty(false);

	private final SimpleBooleanProperty dontShowSaveConfirm = new SimpleBooleanProperty(false);
	
	private final SimpleBooleanProperty autoSaveLast = new SimpleBooleanProperty(true);
	
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

	public void runOnChange(Runnable run) {
		addListeners(run, this.dontShowExitConfirm, this.dontShowSaveConfirm, this.autoSaveLast);
	}

	public SimpleBooleanProperty autoSaveLastProperty() {
		return this.autoSaveLast;
	}
	
	private void addListeners(Runnable run, ObservableValue<?>... obs) {
		for (ObservableValue<?> ob : obs)
			ob.addListener((o, old, newv) -> run.run());
	}
	
}
