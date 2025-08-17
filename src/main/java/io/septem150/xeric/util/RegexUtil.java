package io.septem150.xeric.util;

import java.util.regex.Pattern;

public class RegexUtil {
  public static final Pattern COMBAT_TASK_REGEX =
      Pattern.compile("Congratulations, you've completed an? \\w+ combat task:.*");
  public static final Pattern CLOG_REGEX =
      Pattern.compile("New item added to your collection log: (?<item>.*)");
  public static final Pattern DIARY_REGEX =
      Pattern.compile(
          "Well done! You have completed an? \\w+ task in the .* area\\. Your Achievement"
              + " Diary has been updated");
  public static final Pattern KC_REGEX =
      Pattern.compile(
          "Your (?:subdued |completed )?(?<name>.+?) (?:kill )?count is: (?<count>\\d+)\\.");
  public static final Pattern DELVE_KC_REGEX =
      Pattern.compile("Deep delves completed: (?<count>\\d+)");
  public static final Pattern DELVE_REGEX =
      Pattern.compile(
          "Delve level: (?<wave>\\d+|8\\+ \\((?<deepWave>\\d+)\\)) duration:"
              + " (?<duration>(?:\\d+:)?\\d+:\\d+)(?:\\.\\d+)?(?: \\(new personal best\\)|\\."
              + " Personal best: (?<pb>(?:\\d+:)?\\d+:\\d+)(?:\\.\\d+)?)");
  public static final Pattern CLUE_REGEX =
      Pattern.compile("You have completed (?<count>\\d+) (?<tier>.*) Treasure Trails?\\.");
  public static final Pattern QUEST_REGEX =
      Pattern.compile("Congratulations, you've completed a quest:.*");
}
