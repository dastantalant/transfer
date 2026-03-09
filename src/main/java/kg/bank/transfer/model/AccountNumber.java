package kg.bank.transfer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public class AccountNumber implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String number;

    @Override
    public String toString() {
        return number;
    }
}