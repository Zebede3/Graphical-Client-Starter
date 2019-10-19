package starter.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

// Based on javafx.scene.control.cell.ComboBoxTableCell but this commits the change when losing focus too

public class ComboBoxTableCell<S, T> extends TableCell<S, T> {

	private final ObservableList<T> items;
	
	private ComboBox<T> comboBox;

	@SafeVarargs
	public ComboBoxTableCell(T... items) {
		this.items = FXCollections.observableArrayList(items);
	}

	@Override
	public void startEdit() {
		if (!(this.isEditable() && this.getTableView().isEditable() && this.getTableColumn().isEditable()))
			return;
		if (this.comboBox == null)
			this.comboBox = this.createComboBox();
		this.comboBox.getSelectionModel().select(this.getItem());
		super.startEdit();
		this.setText(null);
		this.setGraphic(this.comboBox);
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		this.setText(getCellText());
		this.setGraphic(null);
	}

	@Override
	public void updateItem(T t, boolean bl) {
		super.updateItem(t, bl);
		updateItem();
	}

	private ComboBox<T> createComboBox() {

		final ComboBox<T> comboBox = new ComboBox<>(this.items);
		comboBox.setMaxWidth(Double.MAX_VALUE);
		comboBox.getSelectionModel().selectedItemProperty().addListener((observableValue, object, object2) -> {
			if (this.isEditing())
				this.commitEdit(object2);
		});

		comboBox.focusedProperty().addListener((obs, old, newv) -> {
			if (!newv)
				this.commitEdit(comboBox.getValue());
		});

		return comboBox;

	}

	private void updateItem() {
		if (this.isEmpty()) {
			this.setText(null);
			this.setGraphic(null);
		} 
		else if (this.isEditing()) {
			if (this.comboBox != null)
				this.comboBox.getSelectionModel().select(this.getItem());
			this.setText(null);
			this.setGraphic(comboBox);
		} 
		else {
			this.setText(getCellText());
			this.setGraphic(null);
		}
	}
	
	private String getCellText() {
		return this.isEmpty() || this.getItem() == null ? "" : String.valueOf(this.getItem());
	}

}
