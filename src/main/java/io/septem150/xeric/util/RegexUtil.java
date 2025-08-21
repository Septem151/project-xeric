package io.septem150.xeric.util;

import java.util.regex.Pattern;

public class RegexUtil {
  public static final Pattern COMBAT_TASK_REGEX =
      Pattern.compile(
          "CA_ID:[0-9,]+\\|Congratulations, you've completed an? (?<tier>\\w+) combat task:"
              + " (?<name>.+?) \\([0-9,]+ points?\\)\\.");
  public static final Pattern CLOG_REGEX =
      Pattern.compile("New item added to your collection log: (?<item>.*)");
  public static final Pattern DIARY_REGEX =
      Pattern.compile(
          "Well done! You have completed an? \\w+ task in the .* area\\. Your Achievement"
              + " Diary has been updated");
  public static final Pattern KC_REGEX =
      Pattern.compile(
          "Your (?:subdued |completed )?(?<name>.+?) (?:kill |success )?count is:"
              + " (?<count>[0-9,]+)\\.");
  public static final Pattern DELVE_KC_REGEX =
      Pattern.compile("(?<name>Deep delves) completed: (?<count>[0-9,]+)");
  public static final Pattern DELVE_REGEX =
      Pattern.compile(
          "Delve level: (?<wave>[0-9,]+|8\\+ \\((?<deepWave>[0-9,]+)\\)) duration:"
              + " (?<duration>(?:[0-9,]+:)?[0-9,]+:[0-9,]+)(?:\\.[0-9,]+)?(?: \\(new personal"
              + " best\\)|\\. Personal best: (?<pb>(?:[0-9,]+:)?[0-9,]+:[0-9,]+)(?:\\.[0-9,]+)?)");
  public static final Pattern CLUE_REGEX =
      Pattern.compile("You have completed (?<count>[0-9,]+) (?<tier>.*) Treasure Trails?\\.");
  public static final Pattern QUEST_REGEX =
      Pattern.compile("Congratulations, you've completed a quest:.*");
}
