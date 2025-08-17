package io.septem150.xeric.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericPluginTest;
import io.septem150.xeric.util.RegexUtil;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegexResultTest {
  private static String REGEX_TESTS_PATH = "regex_tests.json";
  private static Map<String, List<RegexResult>> regexTests;

  private static String CASE_DELVE_KC_REGEX = "DELVE_KC_REGEX";
  private static String CASE_DELVE_REGEX = "DELVE_REGEX";
  private static String CASE_KC_REGEX = "KC_REGEX";
  private static String CASE_CLUE_REGEX = "CLUE_REGEX";

  @BeforeClass
  public static void ImportTests() {
    Gson gson = new Gson();

    try (InputStream in = ProjectXericPluginTest.class.getResourceAsStream(REGEX_TESTS_PATH)) {
      if (in == null) {
        throw new FileNotFoundException(
            String.format("Unable to access resource '%s'", REGEX_TESTS_PATH));
      }

      Type type = new TypeToken<Map<String, List<RegexResult>>>() {}.getType();
      regexTests = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void DelveKcRegex_MatchesTestCases() {
    Assert.assertTrue(regexTests.containsKey(CASE_DELVE_KC_REGEX));
    CheckPatternMatches(RegexUtil.DELVE_KC_REGEX, regexTests.get(CASE_DELVE_KC_REGEX));
  }

  @Test
  public void DelveRegex_MatchesTestCases() {
    Assert.assertTrue(regexTests.containsKey(CASE_DELVE_REGEX));
    CheckPatternMatches(RegexUtil.DELVE_REGEX, regexTests.get(CASE_DELVE_REGEX));
  }

  @Test
  public void KcRegex_MatchesTestCases() {
    Assert.assertTrue(regexTests.containsKey(CASE_KC_REGEX));
    CheckPatternMatches(RegexUtil.KC_REGEX, regexTests.get(CASE_KC_REGEX));
  }

  @Test
  public void ClueRegex_MatchesTestCases() {
    Assert.assertTrue(regexTests.containsKey(CASE_CLUE_REGEX));
    CheckPatternMatches(RegexUtil.CLUE_REGEX, regexTests.get(CASE_CLUE_REGEX));
  }

  private void CheckPatternMatches(Pattern pattern, List<RegexResult> testCases) {
    for (RegexResult testCase : testCases) {
      Matcher matcher = pattern.matcher(testCase.getText());
      Assert.assertTrue(matcher.matches());
      Map<String, String> testGroups = testCase.getGroups();
      for (String group : testGroups.keySet()) {
        Assert.assertEquals(testGroups.get(group), matcher.group(group));
      }
    }
  }
}
