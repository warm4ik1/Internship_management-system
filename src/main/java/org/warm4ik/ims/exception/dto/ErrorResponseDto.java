package org.warm4ik.ims.exception.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ErrorResponseDto {
  private String message;
  private HttpStatus status;
  private LocalDateTime time;

  public ErrorResponseDto(String message, HttpStatus status) {
    this.message = message;
    this.status = status;
    this.time = LocalDateTime.now();
  }
}
