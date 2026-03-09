package kg.bank.transfer.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String from,
        @NotBlank String to,
        @NotNull @DecimalMin(value = "0.01", message = "Сумма должна быть больше нуля") BigDecimal amount,
        String description
) {
}
