package starter.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Pair;
import starter.models.ApplicationConfiguration;
import starter.models.SelectionMode;

// Based on javafx.scene.control.cell.TextFieldTable but this commits the change when losing focus too, not just when pressing enter

// Specially configured for this application to handle selection modes

public class TextFieldTableCell<T> extends TableCell<T, String> {
	
	private static final Map<TableView<?>, Map<String, Object>> tableLocals = new HashMap<>();
	
	private static final String START_DRAG = "startdrag";
	
	private final ApplicationConfiguration config;
	
	private TextField textField;
	
	public TextFieldTableCell(ApplicationConfiguration config) {
		this.config = config;
		setDragHandlers();
	}

	@Override
	public void startEdit() {
		if (!(this.isEditable() && this.getTableView().isEditable() && this.getTableColumn().isEditable()))
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
	
	@SuppressWarnings("unchecked")
	private void setDragHandlers() {
		
        setOnDragDetected(e -> {
        	if (this.config.getSelectionMode() != SelectionMode.CELL)
        		return;
        	this.startFullDrag();
            e.consume();
            putLocal(START_DRAG, new Pair<>(this.getRow(), this.getColumn()));
        });
        setOnMouseDragEntered(e -> {
        	if (this.config.getSelectionMode() != SelectionMode.CELL)
        		return;
        	final Object source = e.getGestureSource();
        	if (!(source instanceof TextFieldTableCell<?>))
        		return;
        	final Pair<Integer, Integer> start = (Pair<Integer, Integer>) getLocal(START_DRAG);
        	if (start == null)
        		return;
			final int startRow = start.getKey();
			final int startCol = start.getValue();
			final int currentRow = getRow();
			final int currentCol = getColumn();
			final int lowerRow = Math.min(startRow, currentRow);
			final int higherRow = Math.max(startRow, currentRow);
			final int lowerCol = Math.min(startCol, currentCol);
			final int higherCol = Math.max(startCol, currentCol);
        	if (!e.isControlDown()) {
        		this.getTableView().getSelectionModel().getSelectedCells().stream().filter(pos -> {
        			final int row = pos.getRow();
        			if (row < lowerRow || row > higherRow)
        				return true;
        			final int col = pos.getColumn();
        			if (col < lowerCol || col > higherCol)
        				return true;
        			return false;
        		}).collect(Collectors.toList()).forEach(p -> {
        			this.getTableView().getSelectionModel().clearSelection(p.getRow(), p.getTableColumn());
        		});
        	}
			for (int i = lowerRow; i <= higherRow; i++) {
				for (int j = lowerCol; j <= higherCol; j++) {
					if (this.getTableView().getColumns().size() < lowerCol)
						break;
					final TableColumn<T, ?> col = this.getTableView().getColumns().get(j);
					this.getTableView().getSelectionModel().select(i, col);
				}
			}
        });
	}
	
	private int getColumn() {
		return this.getTableView().getColumns().indexOf(this.getTableColumn());
	}
	
	private int getRow() {
		return this.getIndex();
	}
	
	public Object getLocal(String key) {
		return tableLocals.computeIfAbsent(this.getTableView(), v -> new HashMap<>()).get(key);
	}
	
	public void putLocal(String key, Object value) {
		tableLocals.computeIfAbsent(this.getTableView(), v -> new HashMap<>()).put(key, value);
	}

}