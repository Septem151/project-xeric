package io.septem150.xeric.task;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Type adapter for JSON serialization/deserialization of Task objects.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
public class TaskTypeAdapter extends TypeAdapter<Task> {
  private static final String NAME = "name";
  private static final String TYPE = "type";
  private static final String TIER = "tier";
  private static final String IMAGE = "image";
  private static final String COMPLETED = "completed";

  @Override
  public void write(JsonWriter out, Task value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    out.beginObject();
    out.name(NAME);
    out.jsonValue('"' + value.getName() + '"');
    out.name(TYPE);
    out.value(value.getType().getName());
    out.name(TIER);
    out.value(value.getTier().getName());
    out.name(IMAGE);
    out.value(value.getImagePath());
    out.name(COMPLETED);
    out.value(value.isCompleted());
    out.endObject();
  }

  @Override
  public Task read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    in.beginObject();
    String taskName = null;
    TaskType taskType = null;
    TaskTier taskTier = null;
    String taskImage = null;
    boolean taskCompleted = false;
    while (in.peek() != JsonToken.END_OBJECT) {
      String name = in.nextName();
      switch (name) {
        case NAME:
          taskName = in.nextString();
          break;
        case TYPE:
          String taskTypeName = in.nextString();
          taskType =
              Arrays.stream(TaskType.values())
                  .filter(tt -> tt.getName().equals(taskTypeName))
                  .findFirst()
                  .orElseThrow();
          break;
        case TIER:
          String taskTierName = in.nextString();
          taskTier =
              Arrays.stream(TaskTier.values())
                  .filter(tt -> tt.getName().equals(taskTierName))
                  .findFirst()
                  .orElseThrow();
          break;
        case IMAGE:
          taskImage = in.nextString();
          break;
        case COMPLETED:
          taskCompleted = in.nextBoolean();
          break;
        default:
      }
    }
    in.endObject();
    if (taskName == null || taskType == null || taskTier == null || taskImage == null) {
      throw new JsonParseException("Missing required property for task");
    }
    Task task;
    switch (taskType) {
      case COLLECT:
        task = new CollectTask(taskName, taskTier, taskImage);
        break;
      case COMBAT:
        task = new CombatTask(taskName, taskTier, taskImage);
        break;
      case DIARY:
        task = new DiaryTask(taskName, taskTier, taskImage);
        break;
      case LEVEL:
        task = new LevelTask(taskName, taskTier, taskImage);
        break;
      case QUEST:
        task = new QuestTask(taskName, taskTier, taskImage);
        break;
      default:
        throw new JsonParseException("Missing required property for task");
    }
    task.setCompleted(taskCompleted);
    return task;
  }
}
