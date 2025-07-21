package com.matchFit.user.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.matchFit.user.entity.Gender;

@Component
public class StringToGenderConverter implements Converter<String, Gender> {

    @Override
    public Gender convert(String source) {
    	System.out.println("StringToGenderConverter called with: [" + source + "]");

        Gender g = Gender.fromLabel(source);
        System.out.println("Converted Gender: " + g);
        return g;    }
}
