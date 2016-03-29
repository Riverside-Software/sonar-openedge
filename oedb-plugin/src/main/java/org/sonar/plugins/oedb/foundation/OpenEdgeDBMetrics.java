/*
 * OpenEdge DB plugin for SonarQube
 * Copyright (C) 2013-2014 Riverside Software
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
package org.sonar.plugins.oedb.foundation;

import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;
import org.sonar.plugins.oedb.api.com.google.common.collect.ImmutableList;

public class OpenEdgeDBMetrics implements Metrics {
  private final static String DOMAIN_OPENEDGE = "OpenEdge";

  public static final String NUM_TABLES_KEY = "OEDB_NUM_TABLES";
  public static final Metric<Integer> NUM_TABLES = new Metric.Builder(NUM_TABLES_KEY, "Tables",
      Metric.ValueType.INT).setDescription("Number of tables").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String NUM_SEQUENCES_KEY = "OEDB_NUM_SEQUENCES";
  public static final Metric<Integer> NUM_SEQUENCES = new Metric.Builder(NUM_SEQUENCES_KEY, "Sequences",
      Metric.ValueType.INT).setDescription("Number of sequences").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String NUM_INDEXES_KEY = "OEDB_NUM_INDEXES";
  public static final Metric<Integer> NUM_INDEXES = new Metric.Builder(NUM_INDEXES_KEY, "Indexes",
      Metric.ValueType.INT).setDescription("Number of indexes").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String NUM_FIELDS_KEY = "OEDB_NUM_FIELDS";
  public static final Metric<Integer> NUM_FIELDS = new Metric.Builder(NUM_FIELDS_KEY, "Fields",
      Metric.ValueType.INT).setDescription("Number of fields").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String NUM_TRIGGERS_KEY = "OEDB_NUM_TRIGGERS";
  public static final Metric<Integer> NUM_TRIGGERS = new Metric.Builder(NUM_TRIGGERS_KEY, "Triggers",
      Metric.ValueType.INT).setDescription("Number of triggers").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).setDomain(DOMAIN_OPENEDGE).create();

  @SuppressWarnings("rawtypes")
  private static final List<Metric> METRICS = ImmutableList.<Metric> of(NUM_TABLES, NUM_SEQUENCES, NUM_INDEXES,
      NUM_FIELDS, NUM_TRIGGERS);

  @SuppressWarnings("rawtypes")
  @Override
  public List<Metric> getMetrics() {
    return METRICS;
  }

}
