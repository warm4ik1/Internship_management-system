package org.warm4ik.ims.exception.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.warm4ik.ims.exception.custom.DeleteWithRelationException;
import org.warm4ik.ims.exception.custom.FileStorageException;
import org.warm4ik.ims.exception.custom.ResourceNotFoundException;
import org.warm4ik.ims.exception.custom.ValidationException;
import org.warm4ik.ims.exception.dto.ErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleEntityNotFound(ResourceNotFoundException ex) {

    String errorMsg = "Сущность с данными параметрами не найдена";

    ErrorResponseDto error = new ErrorResponseDto(errorMsg, HttpStatus.NOT_FOUND);

    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationException(ValidationException ex) {

    String errorMsg = "Ошибка валидации! Проверьте корректность данных.";

    ErrorResponseDto error = new ErrorResponseDto(errorMsg, HttpStatus.BAD_REQUEST);

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({DeleteWithRelationException.class, DataIntegrityViolationException.class})
  public ResponseEntity<ErrorResponseDto> handleForeignKeyException(Exception ex) {
    String errorMsg =
        ex instanceof DeleteWithRelationException
            ? "Невозможно удалить запись: существуют связанные данные"
            : "Операция нарушает целостность данных";

    ErrorResponseDto error = new ErrorResponseDto(errorMsg, HttpStatus.CONFLICT);

    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(FileStorageException.class)
  public ResponseEntity<ErrorResponseDto> handleFileStorageException(FileStorageException ex) {
    String errorMsg = "Ошибка при работе с файловой системой. Попробуйте ещё раз позднее.";

    ErrorResponseDto error = new ErrorResponseDto(errorMsg, HttpStatus.BAD_REQUEST);

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponseDto> handleAuthException(AuthenticationException ex) {

    ErrorResponseDto error = new ErrorResponseDto(ex.getMessage(), HttpStatus.UNAUTHORIZED);

    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {

    ErrorResponseDto error =
        new ErrorResponseDto("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    ErrorResponseDto error =
        new ErrorResponseDto("Некорректные данные ввода", HttpStatus.BAD_REQUEST);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorResponseDto handleAccessDenied(AccessDeniedException ex) {
    return new ErrorResponseDto("Доступ запрещён", HttpStatus.FORBIDDEN);
  }
}
