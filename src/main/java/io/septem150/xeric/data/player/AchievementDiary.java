package io.septem150.xeric.data.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.WordUtils;

@RequiredArgsConstructor
@Getter
public enum AchievementDiary {
  EASY(7933),
  MEDIUM(7934),
  HARD(7935),
  ELITE(7936);

  private final int varbit;

  @Override
  public String toString() {
    return WordUtils.capitalizeFully(this.name());
  }
}
