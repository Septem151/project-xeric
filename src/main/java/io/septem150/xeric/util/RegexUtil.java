package io.septem150.xeric.util;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexUtil {
  public static final String ID_GROUP = "id";
  public static final String NAME_GROUP = "name";
  public static final String COUNT_GROUP = "count";
  public static final Pattern CLOG_REGEX =
      Pattern.compile("New item added to your collection log: (?<" + NAME_GROUP + ">.*)");
  public static final Pattern COMBAT_TASK_REGEX =
      Pattern.compile(
          "CA_ID:(?<"
              + ID_GROUP
              + ">[0-9,]+)\\|Congratulations, you've completed an? \\w+ combat task:"
              + " .+? \\([0-9,]+ points?\\)\\.");
  public static final Pattern DIARY_REGEX =
      Pattern.compile(
          "Well done! You have completed an? \\w+ task in the .* area\\. Your Achievement"
              + " Diary has been updated\\.");
  public static final Pattern KC_REGEX =
      Pattern.compile(
          "Your (?:subdued |completed )?(?<"
              + NAME_GROUP
              + ">.+?) (?:kill |success )?count is:"
              + " (?<"
              + COUNT_GROUP
              + ">[0-9,]+)\\.");
  public static final Pattern DELVE_KC_REGEX =
      Pattern.compile("Deep delves completed: (?<" + COUNT_GROUP + ">[0-9,]+)");
  public static final Pattern CLUE_REGEX =
      Pattern.compile(
          "You have completed (?<"
              + COUNT_GROUP
              + ">[0-9,]+) (?<"
              + NAME_GROUP
              + ">.*) Treasure Trails?\\.");
  public static final Pattern QUEST_REGEX =
      Pattern.compile("Congratulations, you've completed a quest:.*");
}
