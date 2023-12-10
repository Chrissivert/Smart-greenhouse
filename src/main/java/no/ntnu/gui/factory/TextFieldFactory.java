package no.ntnu.gui.factory;

import javafx.scene.control.TextField;
import no.ntnu.gui.builder.TextFieldBuilder;

/**
 * Factory class for creating text fields.
 *
 * @author Chris Sivert Sylte
 * @version 2023.05.02
 */
public class TextFieldFactory {
    /**
     * Create text field with defaults text field builder.
     *
     * @return the text field builder
     */
    public static TextFieldBuilder createTextFieldWithDefaults() {
        return new TextFieldBuilder(new TextField());
    }
}