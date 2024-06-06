package com.alklid.batch.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

public enum DateExtractType {

    //  # 1(metadata), 2(name pattern)
    METADATA(1),
    FILE_NAME_PATTERN(2);

    @Getter
    private int type;

    public static String FILE_NAME_PATTERN_FORMAT = "yyyyMMdd";


    DateExtractType(int type) {
        this.type = type;
    }

    public static DateExtractType of(Integer typeNum) {
        return Arrays.stream(DateExtractType.values())
            .filter(type -> Objects.equals(type.getType(), typeNum))
            .findAny()
            .orElseThrow(() -> new NoSuchElementException("No such type: " + typeNum));
    }

}

