package com.matchFit.post.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.matchFit.post.entity.Sports;

@Component
public class StringToSportsConverter implements Converter<String, Sports> {
	@Override
	public Sports convert(String source) {
		return Sports.fromLabel(source);
	}
}
