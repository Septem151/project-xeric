package io.septem150.xeric.data.player;

import lombok.Data;
import net.runelite.api.Client;
import net.runelite.api.Skill;

@Data
public class Level {
  private String name;
  private int amount;
  private int exp;

  public static Level from(Client client, Skill skill) {
    if (client == null || !client.isClientThread()) {
      throw new RuntimeException("must be on client thread");
    }
    Level level = new Level();
    level.name = skill.getName();
    level.amount = client.getRealSkillLevel(skill);
    level.exp = client.getSkillExperience(skill);
    return level;
  }

  public boolean isAccurate() {
    return amount != 0;
  }
}
