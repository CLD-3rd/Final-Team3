package com.matchFit.post.converter

import com.matchFit.post.entity.Sports
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component


@Component
class StringToSportsConverter : Converter<String, Sports> {
    override fun convert(source: String): Sports =
        Sports.fromLabel(source)
}
