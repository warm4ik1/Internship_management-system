package org.warm4ik.ims.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContactDataValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContactDataValid {
  String message() default "Неверное значение поля value для заданного type";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
