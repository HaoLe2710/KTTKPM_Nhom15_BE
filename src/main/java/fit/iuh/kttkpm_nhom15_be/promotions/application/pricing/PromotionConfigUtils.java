package fit.iuh.kttkpm_nhom15_be.promotions.application.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PromotionConfigUtils {
    private PromotionConfigUtils() {
    }

    public static BigDecimal getBigDecimal(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            return new BigDecimal(text.trim());
        }
        throw new IllegalArgumentException("Invalid decimal value for key: " + key);
    }

    public static Integer getInteger(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text.trim());
        }
        throw new IllegalArgumentException("Invalid integer value for key: " + key);
    }

    public static String getString(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    public static List<String> getStringList(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            List<String> values = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    String text = String.valueOf(item).trim();
                    if (!text.isBlank()) {
                        values.add(text);
                    }
                }
            }
            return values;
        }
        throw new IllegalArgumentException("Invalid list value for key: " + key);
    }
}
