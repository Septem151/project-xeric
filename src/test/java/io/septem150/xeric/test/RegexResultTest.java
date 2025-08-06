package io.septem150.xeric.test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericPluginTest;
import io.septem150.xeric.data.task.Task;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

public class RegexResultTest {
  private static String REGEX_TESTS_PATH = "regex_tests.json";

  @Test
  @DisplayName("Confirm import")
  public void ConfirmImport() {
    Gson gson = new Gson();
    List<RegexResult> testRegex;

    try (InputStream in = ProjectXericPluginTest.class.getResourceAsStream(REGEX_TESTS_PATH)) {
      if (in == null) {
        throw new FileNotFoundException(
            String.format("Unable to access resource '%s'", REGEX_TESTS_PATH));
      }

      Type type = new TypeToken<List<RegexResult>>() {}.getType();
      testRegex = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    for (RegexResult test : testRegex)
    {
      System.out(test.getText());
    }
  }
}