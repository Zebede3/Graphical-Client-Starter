package starter.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import starter.models.AccountColumn;
import starter.models.GitlabPackage;
import starter.models.ProxyManagerColumn;

public class PromptUtil {
	
	public static boolean promptConfirm(Stage stage, Consumer<Scene> onCreation, String title, String headerText, String contentText) {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.initOwner(stage);
		onCreation.accept(alert.getDialogPane().getScene());
		return alert.showAndWait().filter(t -> t == ButtonType.OK).isPresent();
	}
	
	public static void promptInfo(Stage stage, Consumer<Scene> onCreation, String title, String headerText, String contentText) {
		final Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.initOwner(stage);
		onCreation.accept(alert.getDialogPane().getScene());
		alert.showAndWait();
	}

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
	
	public static void promptJcmdTarget(String tribotPath, Stage stage, Consumer<Scene> onCreation, Consumer<Long> onSuccess) {
		Scheduler.executor().submit(() -> {
			JcmdUtil.printJvms(tribotPath, list -> {
				Platform.runLater(() -> {
					final Dialog<Long> dialog = new Dialog<>();
					onCreation.accept(dialog.getDialogPane().getScene());
					dialog.setTitle("Select Client JVM");
					dialog.setHeaderText("Select client JVM target");
					dialog.setContentText(null);
					final ComboBox<String> options = new ComboBox<>();
					options.setMaxWidth(400D);
					options.setItems(FXCollections.observableArrayList(list));
					final VBox box = new VBox();
					final CheckBox onlyTribot = new CheckBox("Only list TRiBot clients");
					onlyTribot.setSelected(true);
					options.itemsProperty().bind(
							Bindings.when(onlyTribot.selectedProperty())
							.then(FXCollections.observableArrayList(list.stream().filter(s -> s.toLowerCase().contains("tribot")).collect(Collectors.toList())))
							.otherwise(FXCollections.observableArrayList(list)));
					box.setAlignment(Pos.CENTER);
					box.getChildren().addAll(onlyTribot, options);
					box.setFillWidth(true);
					box.setSpacing(10);
					dialog.getDialogPane().setContent(box);
					dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
					dialog.initOwner(stage);
					dialog.setResultConverter(dialogButton -> {
					    if (dialogButton == ButtonType.OK && options.getValue() != null) {
					    	try {
						    	final Matcher matcher = Pattern.compile("(\\d+)").matcher(options.getValue());
						    	return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
					    	}
					    	catch (Exception e) {
					    		e.printStackTrace();
					    		return null;
					    	}
					    }
					    return null;
					});
					dialog.showAndWait().ifPresent(onSuccess);
				});
			});
		});
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
	
	public static ImportStrategy promptImportMerge(Stage stage, Consumer<Scene> onCreation, ImportStrategy prev) {
		final ChoiceDialog <ImportStrategy> dialog = new ChoiceDialog<>(prev != null ? prev : ImportStrategy.CREATE_NEW, ImportStrategy.values());
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Select Import Strategy");
		dialog.setHeaderText("Select the import strategy to use");
		dialog.initOwner(stage);
		return dialog.showAndWait().orElse(null);
	}
	
	public static File promptImportFile(Stage stage, FileFormat format) {
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getDirectory());
		chooser.setTitle("Select Import File");
		chooser.getExtensionFilters().add(new ExtensionFilter(format.description() + " Files", "*." + format.extension()));
		final File save = chooser.showOpenDialog(stage);
		return save;
	}
	
	public static AccountColumn[] promptImportRows(Stage stage, Consumer<Scene> onCreation, AccountColumn[] current) {
		final Map<AccountColumn, SimpleBooleanProperty> map = new HashMap<>();
		final ListView<AccountColumn> columns = new ListView<>();
		columns.setCellFactory(CheckBoxListCell.<AccountColumn>forListView(item -> map.computeIfAbsent(item, k -> new SimpleBooleanProperty(ArrayUtil.contains(k, current)))));
		columns.getItems().addAll(AccountColumn.values());
		FXUtil.setDragAndDroppable(columns);
		final Dialog<AccountColumn[]> dialog = new Dialog<>();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Set Import Columns");
		dialog.setHeaderText("Select and order the columns to import");
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
	
	public static File promptExportFile(Stage stage, Consumer<Scene> onCreation, FileFormat format) {
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(FileUtil.getDirectory());
		chooser.setTitle("Save Exported Accounts");
		chooser.getExtensionFilters().add(new ExtensionFilter(format.description() + " Files", "*." + format.extension()));
		return chooser.showSaveDialog(stage);
	}
	
	@SuppressWarnings("unchecked")
	public static String promptAdvancedExportFormat(Stage stage, Consumer<Scene> onCreation) {
		final TableView<AccountColumn> table = new TableView<>();
		final TableColumn<AccountColumn, String> name = new TableColumn<>("Name");
		name.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().toString());
		});
		name.prefWidthProperty().bind(table.widthProperty().divide(2));
		final TableColumn<AccountColumn, String> symbol = new TableColumn<>("Symbol");
		symbol.setCellValueFactory(s -> {
			return new SimpleStringProperty(s.getValue().getSymbol());
		});
		symbol.prefWidthProperty().bind(table.widthProperty().divide(2));
		table.getColumns().addAll(name, symbol);
		for (AccountColumn field : AccountColumn.values()) {
			table.getItems().add(field);
		}
		final Dialog<String> dialog = new Dialog<>();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Set Export Format (Advanced)");
		dialog.setContentText(null);
		final VBox box = new VBox();
		box.setSpacing(10D);
		box.setAlignment(Pos.CENTER);
		final TextField format = new TextField();
		format.setPromptText("Enter export format");
		format.setText("${username}:${password}");
		box.getChildren().addAll(table, format);
		dialog.getDialogPane().setContent(box);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(stage);
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		        return format.getText();
		    }
		    return null;
		});
		return dialog.showAndWait().orElse(null);
	}
	
	public static void promptTribotVersion(Stage stage, Consumer<Scene> onCreation, GitlabPackage[] packages, String path) {
		final Dialog<?> dialog = new Dialog<>();
		onCreation.accept(dialog.getDialogPane().getScene());
		dialog.setTitle("Select TRiBot version");
		dialog.setHeaderText("Select TRiBot version setting");
		dialog.setContentText(null);
		final ComboBox<GitlabPackage> options = new ComboBox<>();
		options.setMaxWidth(400D);
		options.setItems(FXCollections.observableArrayList(packages));
		final VBox box = new VBox();
		final Label label = new Label("This will modify your TRiBot build.gradle file");
		final CheckBox latest = new CheckBox("Always use latest version");
		options.disableProperty().bind(latest.selectedProperty());
		box.setAlignment(Pos.CENTER);
		box.getChildren().addAll(label, latest, options);
		box.setFillWidth(true);
		box.setSpacing(10);
		dialog.getDialogPane().setContent(box);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.initOwner(stage);
		dialog.showAndWait().ifPresent(res -> {
			if (options.getValue() == null && !latest.isSelected()) {
				System.out.println("Nothing selected");
				return;
			}
			final String line = latest.isSelected()
					? "def tribotVersion = project.findProperty('tribotVersion') ?: '+' // TRiBot version to use, default is latest"
					: "def tribotVersion = '" + options.getValue().getVersion() + "'";
			Scheduler.executor().submit(() -> {
				try {
					final List<String> lines = Files.readAllLines(new File(path + File.separator + "tribot-gradle-launcher" + File.separator + "build.gradle").toPath());
					for (int i = 0; i < lines.size(); i++) {
						final String s = lines.get(i);
						if (s.trim().contains("def tribotVersion")) {
							lines.set(i, line);
							final String contents = lines.stream().collect(Collectors.joining(System.lineSeparator()));
							Files.writeString(new File(path + File.separator + "tribot-gradle-launcher" + File.separator + "build.gradle").toPath(), contents);
							System.out.println("Modified build.gradle with tribot version update");
							return;
						}
					}
					System.out.println("Failed to find line containing 'def tribotVersion' to modify in build.gradle");
				}
				catch (IOException e) {
					e.printStackTrace();
					System.out.println("Failed to modify build.gradle");
				}
			});
		});
	}
	
}
