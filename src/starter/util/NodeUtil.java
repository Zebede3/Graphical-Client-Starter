package starter.util;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;

public class NodeUtil {

	private static final String TYPE = "application/x-java-serialized-object";
	private static final DataFormat SERIALIZED_MIME_TYPE;

	static {
		final DataFormat lookup = DataFormat.lookupMimeType(TYPE);
		SERIALIZED_MIME_TYPE = lookup != null ? lookup : new DataFormat(TYPE);
	}
	
	public static <T> void setDragAndDroppable(TableView<T> table) {

		table.setRowFactory(t -> {
			
			final TableRow<T> cell = new TableRow<>();

			cell.setOnDragDetected(e -> {
				if (cell.isEmpty())
					return;
				if (e.getButton() != MouseButton.PRIMARY)
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
					if (dropIndex >= table.getItems().size())
						return;
					
					final int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
					final T draggedItem = table.getItems().remove(draggedIndex);

					table.getItems().add(dropIndex, draggedItem);

					event.setDropCompleted(true);
					table.getSelectionModel().select(dropIndex);
					event.consume();
				}
			});

			return cell;
		});
	}
	
}
