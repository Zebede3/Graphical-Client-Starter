package starter.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import starter.models.ApplicationConfiguration;
import starter.models.SelectionMode;

public class SelectableTableCell<S, T> extends TableCell<S, T> {

	// dont really need this whole thing, just need a way to record the start drag
	// row/col
	private static final Map<TableView<?>, Map<String, Object>> tableLocals = new HashMap<>();

	private static final String START_DRAG = "startdrag";

	private final ApplicationConfiguration config;

	public SelectableTableCell(ApplicationConfiguration config) {
		this.config = config;
		setDragHandlers();
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
			if (!(source instanceof SelectableTableCell))
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
					if (this.getTableView().getColumns().size() <= j)
						break;
					final TableColumn<S, ?> col = this.getTableView().getColumns().get(j);
					this.getTableView().getSelectionModel().select(i, col);
				}
			}
			// keep focus on starting cell
			if (this.getTableView().getColumns().size() > startCol) {
				final TableColumn<S, ?> col = this.getTableView().getColumns().get(startCol);
				this.getTableView().getFocusModel().focus(startRow, col);
			}
		});
	}

	private int getColumn() {
		return this.getTableView().getColumns().indexOf(this.getTableColumn());
	}

	private int getRow() {
		return this.getIndex();
	}

	private Object getLocal(String key) {
		return tableLocals.computeIfAbsent(this.getTableView(), v -> new HashMap<>()).get(key);
	}

	private void putLocal(String key, Object value) {
		tableLocals.computeIfAbsent(this.getTableView(), v -> new HashMap<>()).put(key, value);
	}

}
