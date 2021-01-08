package starter.util;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class FXUtil {

	private static final String TYPE = "application/x-java-serialized-object";
	private static final DataFormat SERIALIZED_MIME_TYPE;

	public static final Font DEFAULT_FONT = new Font("Segoe UI", 12);

	static {
		final DataFormat lookup = DataFormat.lookupMimeType(TYPE);
		SERIALIZED_MIME_TYPE = lookup != null ? lookup : new DataFormat(TYPE);
	}

	@SafeVarargs
	public static <T> ObservableList<T> merge(ObservableList<T>... lists) {

		final ObservableList<T> list = FXCollections.observableArrayList();

		for (ObservableList<T> l : lists) {
			list.addAll(l);
			l.addListener((javafx.collections.ListChangeListener.Change<? extends T> c) -> {
				while (c.next()) {
					if (c.wasRemoved()) {
						list.removeAll(c.getRemoved());
					}
					if (c.wasAdded()) {
						list.addAll(c.getAddedSubList());
					}
				}
			});
		}

		return list;
	}

	public static String colorToCssRgb(Color color) {
		return "rgb(" + color.getRed() * 255 + "," + color.getGreen() * 255 + "," + color.getBlue() * 255 + ");";
	}

	public static void initSpinner(Spinner<Integer> spinner, int min, int max, int start) {
		spinner.setValueFactory(new IntegerSpinnerValueFactory(min, max, start));
		spinner.setEditable(true);
		spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue)
				spinner.increment(0); // won't change value, but will commit editor
		});
	}

	public static void centerOnOpen(Stage parent, Stage child) {
		child.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
			center(parent, child);
		});
	}

	private static void center(Stage parent, Stage child) {
		child.setX(parent.getX() + parent.getWidth() / 2 - child.getWidth() / 2);
		child.setY(parent.getY() + parent.getHeight() / 2 - child.getHeight() / 2);
	}

	public static <T> void setDragAndDroppable(ListView<T> list) {

		final Callback<ListView<T>, ListCell<T>> callback = getListCellFactory(list);

		list.setCellFactory(lv -> {

			final ListCell<T> cell = callback.call(lv);

			cell.setOnDragDetected(e -> {
				if (cell.isEmpty())
					return;
				final Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
				db.setDragView(cell.snapshot(null, null));
				final ClipboardContent cc = new ClipboardContent();
				cc.put(SERIALIZED_MIME_TYPE, cell.getIndex());
				db.setContent(cc);
				e.consume();
			});

			cell.setOnDragOver(event -> {
				final Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {
					if (cell.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
						event.consume();
					}
				}
			});

			cell.setOnDragDropped(event -> {
				final Dragboard db = event.getDragboard();
				if (db.hasContent(SERIALIZED_MIME_TYPE)) {

					final int dropIndex = cell.getIndex();
					if (dropIndex >= list.getItems().size())
						return;
					final int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
					final T draggedTask = list.getItems().remove(draggedIndex);

					list.getItems().add(dropIndex, draggedTask);

					event.setDropCompleted(true);
					list.getSelectionModel().select(dropIndex);
					event.consume();
				}
			});

			return cell;
		});
	}

	public static <T> Callback<ListView<T>, ListCell<T>> getListCellFactory(ListView<T> list) {

		final Callback<ListView<T>, ListCell<T>> factory = list.getCellFactory();

		if (factory != null)
			return factory;

		return v -> {

			final ListCell<T> cell = new ListCell<>();

			cell.textProperty().bind(Bindings.createStringBinding(() -> {
				final T s = cell.getItem();
				if (s == null)
					return null;
				return s.toString();
			}, cell.itemProperty()));

			return cell;
		};

	}

	public static double getStringSize(String s, Font f) {
		final Text text = new Text(s);
		if (f != null) {
			text.setFont(f);
		}
		return text.getBoundsInLocal().getWidth();
	}
	
}
