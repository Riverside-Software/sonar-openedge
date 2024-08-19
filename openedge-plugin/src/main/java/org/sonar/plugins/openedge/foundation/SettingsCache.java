package org.sonar.plugins.openedge.foundation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prorefactor.core.schema.Schema;
import org.sonarsource.api.sonarlint.SonarLintSide;

import eu.rssw.pct.elements.ITypeInfo;

/**
 * Cache RefactorSession objects, that are too expensive to compute every time a file is analyzed. The cache key is the
 * base directory of the project.
 */
@SonarLintSide(lifespan = SonarLintSide.INSTANCE)
public class SettingsCache {
  private final Map<String, List<ITypeInfo>> catalogCache = new HashMap<>();
  private final Map<String, Schema> schemaCache = new HashMap<>();

  public List<ITypeInfo> getCatalogCache(String path) {
    return catalogCache.get(path);
  }

  public void addCatalogCache(String path, List<ITypeInfo> catalog) {
    catalogCache.put(path, catalog);
  }

  public Schema getSchemaCache(String path) {
    return schemaCache.get(path);
  }

  public void addSchemaCache(String path, Schema schema) {
    schemaCache.put(path, schema);
  }
}
