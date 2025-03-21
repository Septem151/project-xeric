package io.septem150.xeric.clog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClogManager {
  private final ClogStore clogStore;

  @Inject
  public ClogManager(ClogStore clogStore) {
    this.clogStore = clogStore;
  }

  public List<ClogItem> getItemsForPage(String name) {
    Optional<ClogPage> clogPage = clogStore.getPageByName(name);
    return clogPage.map(ClogPage::getItems).orElse(null);
  }

  public List<ClogItem> getAllItems() {
    return clogStore.getAllPages().stream()
        .map(ClogPage::getItems)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public List<ClogPage> getAllPages() {
    return clogStore.getAllPages();
  }

  public ClogPage getPageByName(String name) {
    return clogStore.getPageByName(name).orElse(null);
  }
}
