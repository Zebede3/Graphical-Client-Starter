package starter.gui;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import starter.models.ApplicationConfiguration;

// Based on javafx.scene.control.cell.TextFieldTable but this commits the change when losing focus too, not just when pressing enter

public class TextFieldTableCell<T> extends SelectableTableCell<T, String> {
	
	private TextField textField;
	
	public TextFieldTableCell() {}
	
	public TextFieldTableCell(ApplicationConfiguration config) {
		super(config);
	}
	
	public TextField getTextField() {
		if (this.textField == null)
			this.textField = createTextField(this.getItem());
		return this.textField;
	}

	@Override
	public void startEdit() {
		if (!(this.isEditable() && this.getTableRow().isEditable() && this.getTableView().isEditable() && this.getTableColumn().isEditable()))
			return;
		super.startEdit();
		if (this.isEditing()) {
			if (this.textField == null)
				this.textField = createTextField(this.getItem());
			this.textField.setText(this.getItem());
			this.setText(null);
			this.setGraphic(this.textField);
			this.textField.selectAll();
			this.textField.requestFocus();
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		this.setText(this.getItem());
		this.setGraphic(null);
	}

	@Override
	public void updateItem(String s, boolean bl) {
		super.updateItem(s, bl);
		if (this.isEmpty()) {
			this.setText(null);
			this.setGraphic(null);
		} 
		else if (this.isEditing()) {
			if (this.textField != null)
				this.textField.setText(this.getItem());
			this.setText(null);
			this.setGraphic(this.textField);
		} 
		else {
			this.setText(this.getItem());
			this.setGraphic(null);
		}
	}
	
	private TextField createTextField(String s) {
		
		final TextField textfield = new TextField(s);

		textfield.setOnAction(actionEvent -> {
			this.commitEdit(this.textField.getText());
			actionEvent.consume();
		});

		textfield.setOnKeyReleased(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ESCAPE) {
				this.cancelEdit();
				keyEvent.consume();
			}
		});
		
		textfield.focusedProperty().addListener((obs, old, newv) -> {
			if (!newv)
				this.commitEdit(this.textField.getText());
		});
		
		return textfield;
	}

}