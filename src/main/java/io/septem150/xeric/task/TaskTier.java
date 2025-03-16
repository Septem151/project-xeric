package io.septem150.xeric.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Property of a {@link Task} representing the difficulty and point value of a task.
 *
 * <p>Tiers have a name and task point values associated with them, for example:
 *
 * <ul>
 *   <li>{@link TaskTier#TIER_1 Tier 1} awards 1 point each task
 *   <li>{@link TaskTier#TIER_2 Tier 2} awards 2 points each task
 *   <li>etc...
 * </ul>
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 * @see io.septem150.xeric.task.Task
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TaskTier {
  TIER_1("Tier 1", 1),
  TIER_2("Tier 2", 2),
  TIER_3("Tier 3", 3),
  TIER_4("Tier 4", 4),
  TIER_5("Tier 5", 5),
  TIER_6("Tier 6", 6),
  TIER_7("Tier 7", 7),
  TIER_8("Tier 8", 8),
  TIER_9("Tier 9", 9),
  TIER_10("Tier 10", 10);

  private final String name;
  private final int taskValue;
}
