package com.matchFit.user.converter

import com.matchFit.user.entity.Gender
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component


@Component
class StringToGenderConverter : Converter<String, Gender> {
    override fun convert(source: String): Gender =
        Gender.fromLabel(source) ?: throw IllegalArgumentException("Unknown gender: $source")
}
