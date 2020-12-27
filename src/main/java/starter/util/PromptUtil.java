package starter.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import starter.models.AccountColumn;
import starter.models.ProxyManagerColumn;

public class PromptUtil {

	public static Set<Integer> promptRowsToSelectByIndex(Stage stage, Consumer<Scene> onCreation) {
		final TextInputDialog dialog = new TextInputDialog();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Select rows by 'Row Index'");
		dialog.setHeaderText("Row indexes start at 0, use a comma to separate");
		dialog.setContentText("Enter row indexes (ex. 0, 2)");
		dialog.initOwner(stage);
		return dialog.showAndWait().map(text -> {
			return Arrays.stream(text.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.map(Integer::parseInt)
					.collect(Collectors.toSet());
		}).orElse(null);
	}
	
	public static String promptRowsToSelect(String label, Stage stage, Consumer<Scene> onCreation) {
		final TextInputDialog dialog = new TextInputDialog();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Select rows by '" + label + "'");
		dialog.setHeaderText("Select rows by " + label);
		dialog.setContentText("Enter '" + label + "'");
		dialog.initOwner(stage);
		return dialog.showAndWait().orElse(null);
	}
	
	public static Color promptRowsToSelectByColor(Stage stage, Consumer<Scene> onCreation) {
		final Dialog<Color> dialog = new Dialog<>();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Select rows by 'Row Color'");
		dialog.setHeaderText("Select 'Row Color'");
		dialog.setContentText("Select Color");
		final ColorPicker color = new ColorPicker(Color.WHITE);
		final HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().addAll(color);
		dialog.getDialogPane().setContent(box);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(stage);
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK)
		        return color.getValue();
		    return null;
		});
		return dialog.showAndWait().orElse(null);
	}
	
	public static void colorAccounts(Stage stage, Consumer<Scene> onCreation, Consumer<Color> onComplete) {
		final Dialog<Color> dialog = new Dialog<>();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Set Row Color");
		dialog.setHeaderText("Set 'Row Color' for selected accounts");
		dialog.setContentText("Select Color");
		final ColorPicker color = new ColorPicker(Color.WHITE);
		final HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().addAll(color);
		dialog.getDialogPane().setContent(box);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(stage);
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK)
		        return color.getValue();
		    return null;
		});
		dialog.showAndWait().ifPresent(onComplete);
	}

	public static void promptUpdateSelected(String label, Stage stage, Consumer<Scene> onCreation, Consumer<String> onComplete) {
		final TextInputDialog dialog = new TextInputDialog();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Set " + label);
		dialog.setHeaderText("Set '" + label + "' for selected accounts");
		dialog.setContentText("Enter " + label);
		dialog.initOwner(stage);
		dialog.showAndWait().ifPresent(onComplete);
	}
	
	public static AccountColumn[] promptExportRows(Stage stage, Consumer<Scene> onCreation, AccountColumn[] current) {
		final Map<AccountColumn, SimpleBooleanProperty> map = new HashMap<>();
		final ListView<AccountColumn> columns = new ListView<>();
		columns.setCellFactory(CheckBoxListCell.<AccountColumn>forListView(item -> map.computeIfAbsent(item, k -> new SimpleBooleanProperty(ArrayUtil.contains(k, current)))));
		columns.getItems().addAll(AccountColumn.values());
		FXUtil.setDragAndDroppable(columns);
		final Dialog<AccountColumn[]> dialog = new Dialog<>();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Set Export Columns");
		dialog.setHeaderText("Select and order the columns to export");
		dialog.setContentText(null);
		final HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().addAll(columns);
		dialog.getDialogPane().setContent(box);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(stage);
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		        return columns.getItems().stream().filter(i -> map.containsKey(i) && map.get(i).get()).toArray(AccountColumn[]::new);
		    }
		    return null;
		});
		return dialog.showAndWait().orElse(null);
	}
	
	public static ProxyManagerColumn[] promptExportRows(Stage stage, Consumer<Scene> onCreation) {
		final Map<ProxyManagerColumn, SimpleBooleanProperty> map = new HashMap<>();
		final ListView<ProxyManagerColumn> columns = new ListView<>();
		columns.setCellFactory(CheckBoxListCell.<ProxyManagerColumn>forListView(item -> map.computeIfAbsent(item, k -> new SimpleBooleanProperty(true))));
		columns.getItems().addAll(ProxyManagerColumn.values());
		FXUtil.setDragAndDroppable(columns);
		final Dialog<ProxyManagerColumn[]> dialog = new Dialog<>();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Set Export Columns");
		dialog.setHeaderText("Select and order the columns to export");
		dialog.setContentText(null);
		final HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().addAll(columns);
		dialog.getDialogPane().setContent(box);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(stage);
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		        return columns.getItems().stream().filter(i -> map.containsKey(i) && map.get(i).get()).toArray(ProxyManagerColumn[]::new);
		    }
		    return null;
		});
		return dialog.showAndWait().orElse(null);
	}
	
	public static String promptExportDelimiter(Stage stage, Consumer<Scene> onCreation) {
		final TextInputDialog dialog = new TextInputDialog();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Text File Config");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter text to separate each field (ex. :)");
		dialog.initOwner(stage);
		return dialog.showAndWait().orElse(null);
	}
	
}
