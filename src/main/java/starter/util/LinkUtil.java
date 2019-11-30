package starter.util;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LinkUtil {

	public static void showLink(String title, String header, String path, Stage stage, Consumer<Scene> onCreation) {
		final Alert alert = new Alert(AlertType.INFORMATION);
		onCreation.accept(alert.getDialogPane().getScene());
		alert.setTitle(title);
		alert.setHeaderText(header);
		Node node;
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
			final Hyperlink link = new Hyperlink(path);
			link.setOnAction(e -> {
				try {
					Desktop.getDesktop().browse(new URL(path).toURI());
				} 
				catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
			});
			node = link;
		}
		else {
			final TextField text = new TextField();
			text.setEditable(false);
			text.setText(path);
			node = text;
		}
		alert.getDialogPane().setContent(node);
		alert.initOwner(stage);
		alert.showAndWait();
	}
	
}
