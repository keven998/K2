package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 2/10/15.
 */
public class DoubleValidator extends NumberValidator {
    NumberValidator validator;

    public DoubleValidator() {
        this(null);
    }

    public DoubleValidator(NumberValidator validator) {
        this.validator = validator;
    }

    @Override
    public void validate(JsonNode item) {
        assertThat(item.isFloatingPointNumber()).isTrue();
        if (validator != null)
            validator.validate(item);
    }
}
