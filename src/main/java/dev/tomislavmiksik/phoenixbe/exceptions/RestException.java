package dev.tomislavmiksik.phoenixbe.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestException {
    String statusCode;
    String errorMessage;
}
