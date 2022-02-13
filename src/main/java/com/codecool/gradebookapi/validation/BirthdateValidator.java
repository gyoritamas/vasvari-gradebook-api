package com.codecool.gradebookapi.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BirthdateValidator implements ConstraintValidator<Birthdate, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Pattern pattern = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$");
        try {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                return false;
            } else {
                LocalDate parsed = LocalDate.parse(value);

                return parsed.isBefore(LocalDate.now());
            }
        } catch (Exception e) {
            return false;
        }
    }
}
