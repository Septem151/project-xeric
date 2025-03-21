package io.septem150.xeric.clog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.NonNull;

public class InMemoryClogStore implements ClogStore {
  private static final String CLOGS_RES_PATH = "data/clog_pages.json";

  private final List<ClogPage> clogPages;

  @Inject
  public InMemoryClogStore(Gson gson) {
    try (InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(CLOGS_RES_PATH)) {
      if (in == null) {
        throw new FileNotFoundException(
            String.format("Unable to access resource '%s'", CLOGS_RES_PATH));
      }
      TypeToken<List<ClogPage>> type = new TypeToken<>() {};
      clogPages = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type.getType());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<ClogPage> getAllPages() {
    return clogPages;
  }

  @Override
  public Optional<ClogPage> getPageByStructId(int pageStructId) {
    return clogPages.stream()
        .filter(clogPage -> clogPage.getPageStructId() == pageStructId)
        .findFirst();
  }

  @Override
  public Optional<ClogPage> getPageByName(@NonNull String name) {
    return clogPages.stream()
        .filter(clogPage -> clogPage.getName().equalsIgnoreCase(name))
        .findFirst();
  }

  @Override
  public Optional<ClogPage> getPageByName(@NonNull Pattern pattern) {
    return clogPages.stream()
        .filter(clogPage -> pattern.matcher(clogPage.getName()).matches())
        .findFirst();
  }
}
