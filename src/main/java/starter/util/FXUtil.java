package starter.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXUtil {

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
	
}
