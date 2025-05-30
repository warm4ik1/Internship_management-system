package org.warm4ik.ims.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.warm4ik.ims.dto.internship.InternshipContactDataDto;

import java.util.regex.Pattern;

public class ContactDataValidator
    implements ConstraintValidator<ContactDataValid, InternshipContactDataDto> {

  private static final Pattern TELEGRAM_PATTERN = Pattern.compile("^(?:@\\w{5,32}|\\+?\\d{7,15})$");
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile(
          "^(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*)"
              + "@"
              + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+"
              + "[A-Za-z]{2,}$");
  private static final Pattern URL_PATTERN =
      Pattern.compile("^(?:https?://)?" + "[\\w\\-]+(?:\\.[\\w\\-]+)+" + "(?:[/#?].*)?$");

  @Override
  public boolean isValid(InternshipContactDataDto dto, ConstraintValidatorContext ctx) {
    if (dto == null) {
      return true;
    }

    String type = dto.type();
    String value = dto.value();
    if (type == null || value == null) {
      return true;
    }

    String t = type.toUpperCase();

    boolean ok =
        switch (t) {
          case "TELEGRAM" -> TELEGRAM_PATTERN.matcher(value).matches();
          case "EMAIL" -> EMAIL_PATTERN.matcher(value).matches();
          case "WEBSITE" -> URL_PATTERN.matcher(value).matches();
          default -> false;
        };

    if (!ok) {
      ctx.disableDefaultConstraintViolation();
      ctx.buildConstraintViolationWithTemplate(
              "Поле value не соответствует формату для типа: " + type)
          .addPropertyNode("value")
          .addConstraintViolation();
    }
    return ok;
  }
}
