import com.github.sanctum.panther.paste.PasteManager;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class HastebinTest {
    @TempDir
    File temp;

    @Test
    void read() {
        String[] results = PasteManager.getInstance().newHaste().read("test").getAll();
        Arrays.stream(results).forEach(System.out::println);
    }

    @Test
    void newRead() {
        final String multiline = "Here is some text\nover multiple lines";
        InputStream inputStream = new ByteArrayInputStream(multiline.getBytes(StandardCharsets.UTF_8));
        String[] strings1 = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().toArray(String[]::new);
        Arrays.stream(strings1).forEach(System.out::println);
        final File file = new File(temp, "test.html");
        String[] strings2;
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            //noinspection resource
            new FileWriter(file).append(multiline).close();
            strings2 = Resources.readLines(file.toURI().toURL(), StandardCharsets.UTF_8).toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Arrays.stream(strings2).forEach(System.out::println);
        assertArrayEquals(strings1, strings2);
    }
}
