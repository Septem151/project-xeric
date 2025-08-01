package io.septem150.xeric.data.task;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TaskTypeAdapter extends TypeAdapter<Task> {
  private static final String ICON = "icon";
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String TYPE = "type";
  private static final String TIER = "tier";
  private static final String SLAYER_POINTS = "slayerPoints";
  private static final String LEVEL = "level";
  private static final String GOAL = "goal";
  private static final String AMOUNT = "amount";
  private static final String ITEM_IDS = "itemIds";
  private static final String TOTAL = "total";
  private static final String QUEST = "quest";
  private static final String DIARY = "diary";
  private static final String BOSS = "boss";
  private static final String COLLECT = "collect";
  private static final String KC = "kc";
  private static final String CA = "ca";

  @Override
  public void write(JsonWriter out, Task task) throws IOException {
    out.beginObject();
    if (task.getIcon() != null) {
      out.name(ICON).value(task.getIcon());
    }
    out.name(ID).value(task.getId());
    out.name(NAME).value(task.getName());
    out.name(TYPE).value(task.getType());
    out.name(TIER).value(task.getTier());
    if (task.getSlayerPoints() != null) {
      out.name(SLAYER_POINTS).value(task.getSlayerPoints());
    }
    if (task instanceof LevelTask) {
      out.name(LEVEL).value(((LevelTask) task).getLevel());
      out.name(GOAL).value(((LevelTask) task).getGoal());
    } else if (task instanceof CollectTask) {
      out.name(AMOUNT).value(((CollectTask) task).getAmount());
      out.name(ITEM_IDS).beginArray();
      for (int itemId : ((CollectTask) task).getItemIds()) {
        out.value(itemId);
      }
      out.endArray();
    } else if (task instanceof CATask) {
      out.name(TOTAL).value(((CATask) task).getTotal());
    } else if (task instanceof QuestTask) {
      out.name(QUEST).value(((QuestTask) task).getQuest());
    } else if (task instanceof DiaryTask) {
      out.name(DIARY).value(((DiaryTask) task).getDiary());
    } else if (task instanceof KCTask) {
      out.name(BOSS).value(((KCTask) task).getBoss());
      out.name(TOTAL).value(((KCTask) task).getTotal());
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
        case ICON:
        case NAME:
        case TYPE:
          properties.put(key, in.nextString());
          break;
        case ID:
        case TIER:
        case SLAYER_POINTS:
          properties.put(key, in.nextInt());
          break;
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
                  default:
                }
              }
              in.endArray();
              properties.put(key, list);
              break;
            default:
          }
          break;
      }
    }
    in.endObject();

    if (!properties.containsKey(TYPE)) {
      throw new JsonParseException("'" + TYPE + "' key missing from Task json");
    }
    switch (((String) properties.get(TYPE)).toLowerCase()) {
      case COLLECT:
        task = new CollectTask();
        ((CollectTask) task).setAmount((int) properties.get(AMOUNT));
        ((CollectTask) task).setItemIds(new HashSet<>((List<Integer>) properties.get(ITEM_IDS)));
        break;
      case LEVEL:
        task = new LevelTask();
        ((LevelTask) task).setLevel((String) properties.get(LEVEL));
        ((LevelTask) task).setGoal((int) properties.get(GOAL));
        break;
      case CA:
        task = new CATask();
        ((CATask) task).setTotal((int) properties.get(TOTAL));
        break;
      case QUEST:
        task = new QuestTask();
        ((QuestTask) task).setQuest((String) properties.get(QUEST));
        break;
      case DIARY:
        task = new DiaryTask();
        ((DiaryTask) task).setDiary((String) properties.get(DIARY));
        break;
      case KC:
        task = new KCTask();
        ((KCTask) task).setBoss((String) properties.get(BOSS));
        ((KCTask) task).setTotal((int) properties.get(TOTAL));
        break;
      default:
        throw new JsonParseException("Unknown value for Task '" + TYPE + "'");
    }
    task.setType((String) properties.get(TYPE));
    task.setId((Integer) properties.get(ID));
    if (properties.containsKey(ICON)) {
      task.setIcon((String) properties.get(ICON));
    }
    task.setName((String) properties.get(NAME));
    task.setTier((int) properties.get(TIER));
    if (properties.containsKey(SLAYER_POINTS)) {
      task.setSlayerPoints((Integer) properties.get(SLAYER_POINTS));
    }
    return task;
  }
}
