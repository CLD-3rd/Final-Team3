package com.matchFit.user.converter;

import com.matchFit.user.entity.Gender;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToGenderConverter implements Converter<String, Gender> {

    @Override
    public Gender convert(@NonNull String source) {
        Gender gender = Gender.fromLabel(source);
        if (gender == null) {
            throw new IllegalArgumentException("Unknown gender: " + source);
        }
        return gender;
    }
}
