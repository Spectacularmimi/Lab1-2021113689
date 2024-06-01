import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.stream.Collectors;

public class TextProcessor {
    public static String readAndProcessText(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))  // 读取文件
                .map(line -> line.replaceAll("[^A-Za-z\\s]", " ")  // 替换非字母字符为单个空格
                        .replaceAll("\\s+", " "))  // 合并多个空格为一个
                .collect(Collectors.joining(" ")).toLowerCase();  // 转换为小写
    }
}