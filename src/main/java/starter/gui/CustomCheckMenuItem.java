package starter.gui;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.input.MouseEvent;

public class CustomCheckMenuItem extends CustomMenuItem {

	private final CheckBox checkBox;
	
	public CustomCheckMenuItem(String text) {
		this(text, 0);
	}
	
	public CustomCheckMenuItem(String text, double width) {
		super(new CheckBox(text));
		super.setHideOnClick(false);
		this.checkBox = (CheckBox) this.getContent();
		this.checkBox.setStyle("-fx-text-fill: -fx-text-base-color"); // workaround for javafx bug
		this.setOnAction(e -> {
			this.checkBox.setSelected(!this.checkBox.isSelected());
			e.consume();
		});
		// prevent check box from stealing focus
		this.checkBox.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			e.consume();
		});
		if (width > 0) {
			this.checkBox.setPrefWidth(width);
		}
	}
	
	public BooleanProperty selectedProperty() {
		return this.checkBox.selectedProperty();
	}
	
	public boolean isSelected() {
		return this.checkBox.isSelected();
	}

	public void setSelected(boolean b) {
		this.checkBox.setSelected(b);
	}
	
}
