package com.example.demo.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

    /**
     * UTCのInstantを中国時間・日本時間に変換して、
     * 両方を含む文字列を返す。
     *
     * @param utcInstant UTCのInstant
     * @return フォーマット済みの日時文字列（中国時間と日本時間を含む）
     */
    public static String formatTimeWithZones(Instant utcInstant) {
        if (utcInstant == null) {
            return "";
        }

        // 中国時間（Asia/Shanghai）
        ZonedDateTime chinaTime = utcInstant.atZone(ZoneId.of("Asia/Shanghai"));
        // 日本時間（Asia/Tokyo）
        ZonedDateTime japanTime = utcInstant.atZone(ZoneId.of("Asia/Tokyo"));

        // フォーマット
        String chinaStr = chinaTime.format(FORMATTER);
        String japanStr = japanTime.format(FORMATTER);

        // 返却文字列の例（改行あり）
        return String.format("【中国時間】%s\n【日本時間】%s", chinaStr, japanStr);
    }
}
