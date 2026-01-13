package com.matchFit.user.entity

enum class Gender(val label: String) {
    MALE("남성"),
    FEMALE("여성"),
    ALL("남녀 모두");

    companion object {
        private val labelMap: Map<String, Gender> = entries.associateBy { it.label }

        fun fromLabel(label: String?): Gender? {
            if (label.isNullOrBlank()) {
                return null
            }
            return labelMap[label]
                ?: throw IllegalArgumentException("Unknown gender: $label")
        }
    }
}
