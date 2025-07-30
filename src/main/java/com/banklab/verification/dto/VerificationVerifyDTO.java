package com.banklab.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationVerifyDTO {
    private String target;
    private String code;
    private Boolean isEmail;
}
