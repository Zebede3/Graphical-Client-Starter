package starter.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import starter.models.ObservableStack;
import starter.models.StarterConfiguration;

public class UndoHandler {

	private final ObservableStack<StarterConfiguration> undo = new ObservableStack<>();
	private final ObservableStack<StarterConfiguration> redo = new ObservableStack<>();
	
	private final BooleanBinding undoEmpty = Bindings.createBooleanBinding(() -> {
		return this.undo.isEmpty();
	}, this.undo);
	
	private final BooleanBinding redoEmpty = Bindings.createBooleanBinding(() -> {
		return this.redo.isEmpty();
	}, this.redo);
	
	private final SimpleObjectProperty<StarterConfiguration> model;
	
	public UndoHandler(SimpleObjectProperty<StarterConfiguration> model) {
		this.model = model;
	}
	
	// Note that currently undo/redo do not take into account selecting checkbox table cells manually
	
	public void cacheAccounts() {
		this.undo.push(this.model.get().copy());
		this.redo.clear();
	}
	
	public void undoAccounts() {
		if (this.undo.isEmpty())
			return;
		this.redo.push(this.model.get().copy());
		this.model.get().getAccounts().setAll(this.undo.pop().getAccounts());
	}
	
	public void redoAccounts() {
		if (this.redo.isEmpty())
			return;
		this.undo.push(this.model.get().copy());
		this.model.get().getAccounts().setAll(this.redo.pop().getAccounts());
	}
	
	public BooleanBinding redoEmptyBinding() {
		return this.redoEmpty;
	}
	
	public BooleanBinding undoEmptyBinding() {
		return this.undoEmpty;
	}
	
}
