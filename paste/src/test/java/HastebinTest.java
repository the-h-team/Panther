import com.github.sanctum.panther.paste.PasteManager;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class HastebinTest {
    @Test
    void read() {
        String[] results = PasteManager.getInstance().newHaste().read("test").getAll();
        Arrays.stream(results).forEach(System.out::println);
    }

    @Test
    void fakeRead() {
        InputStream inputStream = new ByteArrayInputStream("Here is some text\0\nover multiple lines".getBytes(StandardCharsets.UTF_8));
        Arrays.stream(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().toArray(String[]::new))
                .forEach(System.out::println);
    }
}
