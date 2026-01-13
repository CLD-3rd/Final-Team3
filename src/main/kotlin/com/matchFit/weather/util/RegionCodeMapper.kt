package com.matchFit.weather.util


object RegionCodeMapper {
    private val regionCodeMap = mapOf(
        "SEOUL" to "11B10101",
        "GYEONGGI" to "11B00000",
        "INCHEON" to "11B20201",
        "GANGWON" to "11D10000",
        "DAEJEON" to "11C20000",
        "DAEGU" to "11H10000",
        "GWANGJU" to "11F20000",
        "ULSAN" to "11H20000",
        "BUSAN" to "11H20201",
        "SEJONG" to "11C20404",
        "CHUNGNAM" to "11C20101",
        "CHUNGBUK" to "11C10301",
        "JEONBUK" to "11F10201",
        "JEONNAM" to "11F20401",
        "GYEONGBUK" to "11H10101",
        "GYEONGNAM" to "11H20301",
        "JEJU" to "11G00201"
    )

    fun getRegionCode(regionKey: String): String? =
        regionCodeMap[regionKey.uppercase()]

    private val regionGridMap = mapOf(
        "SEOUL" to intArrayOf(60, 127),
        "BUSAN" to intArrayOf(98, 76),
        "DAEGU" to intArrayOf(89, 90),
        "INCHEON" to intArrayOf(55, 124),
        "GWANGJU" to intArrayOf(58, 74),
        "DAEJEON" to intArrayOf(67, 100),
        "ULSAN" to intArrayOf(102, 84),
        "SEJONG" to intArrayOf(67, 100),
        "GANGWON" to intArrayOf(73, 134),
        "GYEONGGI" to intArrayOf(60, 120),
        "CHUNGBUK" to intArrayOf(69, 107),
        "CHUNGNAM" to intArrayOf(66, 103),
        "GYEONGBUK" to intArrayOf(89, 91),
        "GYEONGNAM" to intArrayOf(92, 77),
        "JEONBUK" to intArrayOf(63, 91),
        "JEONNAM" to intArrayOf(51, 67),
        "JEJU" to intArrayOf(52, 38)
    )

    fun getNx(regionCode: String): String =
        (regionGridMap[regionCode] ?: intArrayOf(60, 127))[0].toString()

    fun getNy(regionCode: String): String =
        (regionGridMap[regionCode] ?: intArrayOf(60, 127))[1].toString()
}
