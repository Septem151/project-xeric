package io.septem150.xeric.data;

import lombok.Data;
import net.runelite.api.Client;

@Data
public class ClogItem {
  private int id;
  private String name;

  public static ClogItem from(Client client, int id) {
    if (client == null || !client.isClientThread()) {
      throw new RuntimeException("must be on client thread");
    }
    ClogItem item = new ClogItem();
    item.id = id;
    item.name = client.getItemDefinition(id).getMembersName();
    return item;
  }
}
