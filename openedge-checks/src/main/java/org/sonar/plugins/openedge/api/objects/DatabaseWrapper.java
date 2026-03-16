/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.api.objects;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.prorefactor.core.schema.Constants;
import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.MetaSchemaProvider;

import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.pct.mapping.OpenEdgeVersion;

public class DatabaseWrapper implements IDatabase {
  private final DatabaseDescription dbDesc;
  private final List<ITable> tblList;

  public DatabaseWrapper(DatabaseDescription dbDesc) {
    this(dbDesc, null);
  }

  public DatabaseWrapper(@Nonnull DatabaseDescription dbDesc, @Nullable OpenEdgeVersion metaschema) {
    this.dbDesc = Objects.requireNonNull(dbDesc);
    Set<ITable> set = new HashSet<>();
    for (var tbl : dbDesc.getTables()) {
      set.add(new TableWrapper(this, tbl));
    }
    if (metaschema != null) {
      set.addAll(MetaSchemaProvider.getMetaSchema(this, metaschema));
    }
    this.tblList = set.stream().sorted(Constants.TABLE_NAME_ORDER).toList();
  }

  @Nonnull
  public DatabaseDescription getDbDesc() {
    return dbDesc;
  }

  @Override
  public String getName() {
    return dbDesc.getDbName();
  }

  @Override
  public List<ITable> getTableSet() {
    return tblList;
  }

}
