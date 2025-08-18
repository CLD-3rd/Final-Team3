package com.matchFit.weather.util;

import java.util.Map;

public class RegionCodeMapper {

    private static final Map<String, String> REGION_CODE_MAP = Map.ofEntries(
    		Map.entry("SEOUL", "11B10101"),
            Map.entry("GYEONGGI", "11B00000"),
            Map.entry("INCHEON", "11B20201"),
            Map.entry("GANGWON", "11D10000"),
            Map.entry("DAEJEON", "11C20000"),
            Map.entry("DAEGU", "11H10000"),
            Map.entry("GWANGJU", "11F20000"),
            Map.entry("ULSAN", "11H20000"),
            Map.entry("BUSAN", "11H20201"),
            Map.entry("SEJONG", "11C20404"),
            Map.entry("CHUNGNAM", "11C20101"),
            Map.entry("CHUNGBUK", "11C10301"),
            Map.entry("JEONBUK", "11F10201"),
            Map.entry("JEONNAM", "11F20401"),
            Map.entry("GYEONGBUK", "11H10101"),
            Map.entry("GYEONGNAM", "11H20301"),
            Map.entry("JEJU", "11G00201")
    );

    public static String getRegionCode(String regionKey) {
        return REGION_CODE_MAP.get(regionKey.toUpperCase());
    }
    
    private static final Map<String, int[]> regionGridMap = Map.ofEntries(
            Map.entry("SEOUL", new int[]{60, 127}),
            Map.entry("BUSAN", new int[]{98, 76}),
            Map.entry("DAEGU", new int[]{89, 90}),
            Map.entry("INCHEON", new int[]{55, 124}),
            Map.entry("GWANGJU", new int[]{58, 74}),
            Map.entry("DAEJEON", new int[]{67, 100}),
            Map.entry("ULSAN", new int[]{102, 84}),
            Map.entry("SEJONG", new int[]{67, 100}),  // 대전과 동일 좌표
            Map.entry("GANGWON", new int[]{73, 134}),
            Map.entry("GYEONGGI", new int[]{60, 120}),
            Map.entry("CHUNGBUK", new int[]{69, 107}),
            Map.entry("CHUNGNAM", new int[]{66, 103}),
            Map.entry("GYEONGBUK", new int[]{89, 91}),
            Map.entry("GYEONGNAM", new int[]{92, 77}),
            Map.entry("JEONBUK", new int[]{63, 91}),
            Map.entry("JEONNAM", new int[]{51, 67}),
            Map.entry("JEJU", new int[]{52, 38})
        );

        private String getNx(String regionCode) {
            int[] coords = regionGridMap.get(regionCode);
            if (coords == null) {
                // 기본값 혹은 예외처리
                coords = new int[]{60, 127};  // 서울 기본값
            }
            return String.valueOf(coords[0]);
        }

        private String getNy(String regionCode) {
            int[] coords = regionGridMap.get(regionCode);
            if (coords == null) {
                coords = new int[]{60, 127};
            }
            return String.valueOf(coords[1]);
        }
}