package com.matchFit.post.util;

import java.util.Map;

public class RegionCodeMapper {

    private static final Map<String, String> REGION_CODE_MAP = Map.ofEntries(
        Map.entry("SEOUL", "11B10101"),
        Map.entry("GYEONGGI", "11B00000"),
        Map.entry("INCHEON", "11B00000"),
        Map.entry("GANGWON", "11D10000"),
        Map.entry("DAEJEON", "11C20000"),
        Map.entry("DAEGU", "11H10000"),
        Map.entry("GWANGJU", "11F20000"),
        Map.entry("ULSAN", "11H20000"),
        Map.entry("BUSAN", "11H20000"),
        Map.entry("SEJONG", "11C20000"),
        Map.entry("CHUNGNAM", "11C20000"),
        Map.entry("CHUNGBUK", "11C10000"),
        Map.entry("JEONBUK", "11F10000"),
        Map.entry("JEONNAM", "11F20000"),
        Map.entry("GYEONGBUK", "11H10000"),
        Map.entry("GYEONGNAM", "11H20000"),
        Map.entry("JEJU", "11G00000")
    );

    public static String getRegionCode(String regionKey) {
        return REGION_CODE_MAP.get(regionKey.toUpperCase());
    }
}