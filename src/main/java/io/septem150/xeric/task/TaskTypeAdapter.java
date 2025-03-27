package io.septem150.xeric.task;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskTypeAdapter extends TypeAdapter<Task> {

  @Override
  public void write(JsonWriter out, Task value) throws IOException {
    out.beginObject();
    out.name("icon").jsonValue(value.getIcon());
    out.name("id").jsonValue(value.getId());
    out.name("name").jsonValue(value.getName());
    out.name("type").jsonValue(value.getType());
    out.name("tier").value(value.getTier());
    if (value instanceof LevelTask) {
      out.name("level").jsonValue(((LevelTask) value).getLevel());
      out.name("goal").value(((LevelTask) value).getGoal());
    } else if (value instanceof CollectTask) {
      out.name("amount").value(((CollectTask) value).getAmount());
      out.name("itemIds").beginArray();
      for (int itemId : ((CollectTask) value).getItemIds()) {
        out.value(itemId);
      }
      out.endArray();
    } else if (value instanceof CATask) {
      out.name("total").value(((CATask) value).getTotal());
    }
    out.endObject();
  }

  @Override
  public Task read(JsonReader in) throws IOException {
    Task task = null;
    Map<String, Object> properties = new HashMap<>();
    in.beginObject();
    while (in.hasNext()) {
      String key = in.nextName();
      switch (key) {
        case "icon":
        case "id":
        case "name":
        case "type":
          properties.put(key, in.nextString());
          break;
        case "tier":
          properties.put(key, in.nextInt());
        default:
          JsonToken token = in.peek();
          switch (token) {
            case STRING:
              properties.put(key, in.nextString());
              break;
            case NUMBER:
              properties.put(key, in.nextInt());
              break;
            case BEGIN_ARRAY:
              in.beginArray();
              List<Object> list = new ArrayList<>();
              while (in.hasNext()) {
                JsonToken arrToken = in.peek();
                switch (arrToken) {
                  case STRING:
                    list.add(in.nextString());
                    break;
                  case NUMBER:
                    list.add(in.nextInt());
                    break;
                }
              }
              in.endArray();
              properties.put(key, list);
              break;
          }
          break;
      }
    }
    in.endObject();

    if (!properties.containsKey("type")) {
      throw new JsonParseException("'type' key missing from Task json");
    }
    switch (((String) properties.get("type")).toLowerCase()) {
      case "collect":
        task = new CollectTask();
        ((CollectTask) task).setAmount((int) properties.get("amount"));
        ((CollectTask) task).setItemIds((List<Integer>) properties.get("itemIds"));
        break;
      case "level":
        task = new LevelTask();
        ((LevelTask) task).setLevel((String) properties.get("level"));
        ((LevelTask) task).setGoal((int) properties.get("goal"));
        break;
      case "ca":
        task = new CATask();
        ((CATask) task).setTotal((int) properties.get("total"));
        break;
      default:
        throw new JsonParseException("Unknown value for Task 'type'");
    }
    task.setType((String) properties.get("type"));
    task.setId((String) properties.get("id"));
    task.setIcon((String) properties.get("icon"));
    task.setName((String) properties.get("name"));
    task.setTier((int) properties.get("tier"));
    return task;
  }
}
