package no.ntnu.gui.builder;

import javafx.scene.control.TextField;

import java.util.function.Consumer;

/**
 * Class for building text fields
 *
 * @version 2023.05.18
 * @author Brage Solem and Chris Sivert Sylte
 */
public class TextFieldBuilder {

    private TextField textField;

    /**
     * Instantiates a new Text field builder.
     *
     * @param textField the text field
     */
    public TextFieldBuilder(TextField textField) {
        this.textField = textField;
    }

    /**
     * With prompt text Textfield builder.
     *
     * @param promptText the prompt text
     * @return the text field builder
     */
    public TextFieldBuilder withPromptText(String promptText) {
        textField.setPromptText(promptText);
        return this;
    }

    /**
     * With max length text field builder.
     *
     * @param maxLength the max length
     * @return the text field builder
     */
    public TextFieldBuilder withMaxLength(int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > maxLength) {
                textField.setText(oldValue);
            }
        });
        return this;
    }

    /**
     * Add numeric only filter text field builder.
     *
     * @return the text field builder
     */
    public TextFieldBuilder addNumericOnlyFilter() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        return this;
    }


    /**
     * Set preferred size text field builder.
     *
     * @param maxSize the max size
     * @return the text field builder
     */
    public TextFieldBuilder setPreferedSize (int maxSize){
        textField.setMaxWidth(maxSize);
        return this;
    }

    /**
     * Build text field.
     *
     * @return the text field
     */
    public TextField build(){
        return textField;
    }
}

