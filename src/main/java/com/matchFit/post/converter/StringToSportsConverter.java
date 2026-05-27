package com.matchFit.post.converter;

import com.matchFit.post.entity.Sports;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToSportsConverter implements Converter<String, Sports> {

    @Override
    public Sports convert(@NonNull String source) {
        Sports sports = Sports.fromLabel(source);
        if (sports == null) {
            throw new IllegalArgumentException("Unknown sports: " + source);
        }
        return sports;
    }
}
