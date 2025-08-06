package io.septem150.xeric.test;

import java.util.Map;
import lombok.Data;

@Data
public class RegexResult {
  private String text;
  private Map<String, String> groups;
}
