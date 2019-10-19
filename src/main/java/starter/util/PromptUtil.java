package starter.util;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
	
}
