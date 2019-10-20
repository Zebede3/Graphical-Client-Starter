package starter.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.util.Callback;
import starter.models.ApplicationConfiguration;

// based on javafx.scene.control.cell.CheckBoxTableCell but extends SelectableTableCell

public class CheckBoxTableCell<S, T> extends SelectableTableCell<S, T> {

	private final Callback<Integer, ObservableValue<Boolean>> getSelectedProperty;
	private final CheckBox checkBox;

	private ObservableValue<Boolean> booleanProperty;

	public CheckBoxTableCell(ApplicationConfiguration config, Callback<Integer, ObservableValue<Boolean>> getSelectedProperty) {
		super(config);
		this.getStyleClass().add("check-box-table-cell");
		this.checkBox = new CheckBox();
		setGraphic(null);
		this.getSelectedProperty = getSelectedProperty;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateItem(T item, boolean empty) {
		
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} 
		else {

			setGraphic(this.checkBox);

			if (this.booleanProperty instanceof BooleanProperty)
				this.checkBox.selectedProperty().unbindBidirectional((BooleanProperty) this.booleanProperty);
			
			final ObservableValue<?> obsValue = this.getSelectedProperty.call(getIndex());
			if (obsValue instanceof BooleanProperty) {
				this.booleanProperty = (ObservableValue<Boolean>) obsValue;
				this.checkBox.selectedProperty().bindBidirectional((BooleanProperty) this.booleanProperty);
			}

			this.checkBox.disableProperty().bind(Bindings.not(getTableView().editableProperty()
					.and(getTableColumn().editableProperty()).and(editableProperty())));
		}
	}

}
