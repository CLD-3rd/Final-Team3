package com.matchFit.post.entity

enum class Sports(val label: String) {
    FOOTBALL("축구"),
    BASKETBALL("농구"),
    BADMINTON("배드민턴"),
    TABLE_TENNIS("탁구"),
    VOLLEYBALL("배구"),
    TENNIS("테니스");

    companion object {
        private val labelMap: Map<String, Sports> = entries.associateBy { it.label }

        fun fromLabel(label: String?): Sports? {
            if (label.isNullOrBlank()) {
                return null
            }
            return labelMap[label]
                ?: throw IllegalArgumentException("Unknown sports: $label")
        }
    }
}
