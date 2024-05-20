import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Util {
    private static final Gson gson = new Gson();
    // 方法：将Map序列化为String
    public static String serializeMap(Map<String, List<List<String>>> map) {
        return gson.toJson(map);
    }

    // 方法：将String反序列化为Map
    public static Map<String, List<List<String>>> deserializeMap(String json) {
        Type type = new TypeToken<Map<String, List<List<String>>>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public static String listToString(List<String> list) {
        return String.join(",", list);
    }
    public static List<String> stringToList(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return Arrays.asList(str.split(","));
    }
}
