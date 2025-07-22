package com.banklab.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    MALE("M"),
    FEMALE("F"),
    NONE("N");

    private final String code;

    public static Gender fromCode(String code) {
        for (Gender g : values()) {
            if (g.code.equalsIgnoreCase(code)) return g;
        }
        throw new IllegalArgumentException("Unknown gender code: " + code);
    }
}
