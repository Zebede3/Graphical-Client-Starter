package starter.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.paint.Color;

public class FXUtil {

	@SafeVarargs
	public static <T> ObservableList<T> merge(ObservableList<T>... lists) {
		
        final ObservableList<T> list = FXCollections.observableArrayList();
        
        for (ObservableList<T> l : lists) {
            list.addAll(l);
            l.addListener((javafx.collections.ListChangeListener.Change<? extends T> c) -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        list.addAll(c.getAddedSubList());
                    }
                    if (c.wasRemoved()) {
                        list.removeAll(c.getRemoved());
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
	
}
