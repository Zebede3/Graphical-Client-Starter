package starter.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class AutoCompleteTextField extends TextField {

	private final SortedSet<String> entries;
	private final ContextMenu entriesPopup;

	public AutoCompleteTextField() {
		this.entries = new TreeSet<>();
		this.entriesPopup = new ContextMenu();
		setListener();
	}

	public AutoCompleteTextField(String text) {
		this();
		this.setText(text);
	}

	private void setListener() {
		
		textProperty().addListener((observable, oldValue, newValue) -> {
			tryShow();
		});

		setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.UP) {
				tryShow();
				if (this.entriesPopup.isShowing()) {
					this.entriesPopup.getSkin().getNode().lookup(".menu-item").requestFocus();
				}
			}
		});
		
		// this happens when the node is first shown, after being layed out
		this.localToSceneTransformProperty().addListener((obs, old, newv) -> {
			this.entriesPopup.hide();
			tryShow();
		});
	}

	public void tryShow() {
		if (!this.isVisible() || this.getScene() == null || this.getScene().getWindow() == null || this.getHeight() == 0 || getText() == null) {
			this.entriesPopup.hide();
			return;
		}
		final String enteredText = getText();
		final List<String> filteredEntries = this.entries.stream()
				.filter(e -> e.toLowerCase().contains(enteredText.toLowerCase())).collect(Collectors.toList());
		if (!filteredEntries.isEmpty()) {
			populatePopup(filteredEntries, enteredText);
			if (!this.entriesPopup.isShowing()) {
				this.entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
			}
		} 
		else {
			this.entriesPopup.hide();
		}
	}

	private void populatePopup(List<String> searchResult, String searchRequest) {
		final List<CustomMenuItem> menuItems = new LinkedList<>();
		final int maxEntries = 10;
		final int count = Math.min(searchResult.size(), maxEntries);
		for (int i = 0; i < count; i++) {
			final String result = searchResult.get(i);
			final Label entryLabel = new Label();
			entryLabel.setGraphic(buildTextFlow(result, searchRequest));
			entryLabel.setPrefHeight(14);
			entryLabel.setPrefWidth(150D);
			entryLabel.setMaxWidth(150D);
			final CustomMenuItem item = new CustomMenuItem(entryLabel, true);
			menuItems.add(item);
			item.setOnAction(actionEvent -> {
				setText(result);
				positionCaret(result.length());
				entriesPopup.hide();
			});
		}

		this.entriesPopup.getItems().clear();
		final Label entryLabel = new Label();
		entryLabel.setGraphic(new TextFlow(new Text("Imported from TRiBot:")));
		entryLabel.setPrefHeight(10);
		final CustomMenuItem item = new CustomMenuItem(entryLabel, true);
		item.setDisable(true);
		this.entriesPopup.getItems().add(item);
		this.entriesPopup.getItems().add(new SeparatorMenuItem());
		this.entriesPopup.getItems().addAll(menuItems);
	}

	public SortedSet<String> getEntries() {
		return entries;
	}

	private static TextFlow buildTextFlow(String text, String filter) {
		if (filter == null || filter.isEmpty()) {
			return new TextFlow(new Text(text));
		}
		final int filterIndex = text.toLowerCase().indexOf(filter.toLowerCase());
		final Text textBefore = new Text(text.substring(0, filterIndex));
		final Text textAfter = new Text(text.substring(filterIndex + filter.length()));
		final Text textFilter = new Text(text.substring(filterIndex, filterIndex + filter.length()));
		textFilter.setFill(Color.ORANGE);
		textFilter.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
		return new TextFlow(textBefore, textFilter, textAfter);
	}

}