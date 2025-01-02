/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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
package org.sonar.plugins.openedge.foundation;

import java.util.List;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.google.common.collect.ImmutableList;

public class OpenEdgeMetrics implements Metrics {
  public static final String DOMAIN_OPENEDGE = "OpenEdge";
  public static final String DOMAIN_OPENEDGE_DB = "OpenEdgeDB";

  // ***********************
  // CoreMetrics.DOMAIN_SIZE
  // ***********************

  public static final Metric<Integer> CLASSES = new Metric.Builder("OE_CLASSES", "OpenEdge classes",
      Metric.ValueType.INT) //
        .setDescription("Number of classes") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(CoreMetrics.DOMAIN_SIZE) //
        .create();

  public static final Metric<Integer> PACKAGES = new Metric.Builder("OE_PACKAGES", "OpenEdge packages",
      Metric.ValueType.INT) //
        .setDescription("Number of packages") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(CoreMetrics.DOMAIN_SIZE) //
        .create();

  public static final Metric<Integer> PROCEDURES = new Metric.Builder("PROCEDURES", "Procedures (.p)",
      Metric.ValueType.INT) //
        .setDescription("Number of procedures") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(CoreMetrics.DOMAIN_SIZE) //
        .create();

  public static final Metric<Integer> INCLUDES = new Metric.Builder("INCLUDES", "Includes (.i)", Metric.ValueType.INT) //
    .setDescription("Number of includes files") //
    .setDirection(Metric.DIRECTION_NONE) //
    .setQualitative(false) //
    .setDomain(CoreMetrics.DOMAIN_SIZE) //
    .create();

  public static final Metric<Integer> WINDOWS = new Metric.Builder("WINDOWS", "Windows (.w)", Metric.ValueType.INT) //
    .setDescription("Number of GUI components") //
    .setDirection(Metric.DIRECTION_NONE) //
    .setQualitative(false) //
    .setDomain(CoreMetrics.DOMAIN_SIZE) //
    .create();

  public static final Metric<Integer> INTERNAL_PROCEDURES = new Metric.Builder("OE_NUM_INT_PROCS",
      "Internal procedures", Metric.ValueType.INT) //
        .setDescription("Number of internal procedures") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(CoreMetrics.DOMAIN_SIZE) //
        .create();

  public static final Metric<Integer> INTERNAL_FUNCTIONS = new Metric.Builder("OE_NUM_INT_FUNCS", "Internal functions",
      Metric.ValueType.INT) //
        .setDescription("Number of internal functions") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(CoreMetrics.DOMAIN_SIZE) //
        .create();

  public static final Metric<Integer> METHODS = new Metric.Builder("OE_NUM_METHODS", "Methods", Metric.ValueType.INT) //
    .setDescription("Number of methods") //
    .setDirection(Metric.DIRECTION_NONE) //
    .setQualitative(false) //
    .setDomain(CoreMetrics.DOMAIN_SIZE) //
    .create();

  public static final Metric<Integer> DIRECTIVES = new Metric.Builder("OE_NUM_DIRECTIVES", "Proparse Directives", Metric.ValueType.INT) //
      .setDescription("Number of Proparse Directives") //
      .setDirection(Metric.DIRECTION_NONE) //
      .setQualitative(false) //
      .setDomain(CoreMetrics.DOMAIN_SIZE) //
      .create();

  // *******************************
  // OpenEdgeMetrics.DOMAIN_OPENEDGE
  // *******************************

  public static final Metric<Integer> NUM_TRANSACTIONS = new Metric.Builder("NUM_TRANSACTIONS",
      "Number of transaction blocks", Metric.ValueType.INT) //
        .setDescription("Number of transaction blocks") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE) //
        .create();

  public static final Metric<Integer> SHR_TT = new Metric.Builder("OE_SHR_TT", "New shared temp-tables",
      Metric.ValueType.INT) //
        .setDescription("Number of new shared temp-tables") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE) //
        .create();

  public static final Metric<Integer> SHR_DS = new Metric.Builder("OE_SHR_DS", "New shared datasets",
      Metric.ValueType.INT) //
        .setDescription("Number of new shared datasets") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE) //
        .create();

  public static final Metric<Integer> SHR_VAR = new Metric.Builder("OE_SHR_VAR", "New shared variables",
      Metric.ValueType.INT) //
        .setDescription("Number of new shared variables") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE) //
        .create();

  // **********************************
  // OpenEdgeMetrics.DOMAIN_OPENEDGE_DB
  // **********************************

  public static final Metric<Integer> NUM_TABLES = new Metric.Builder("OEDB_NUM_TABLES", "DB tables",
      Metric.ValueType.INT) //
        .setDescription("Number of tables") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE_DB) //
        .create();

  public static final Metric<Integer> NUM_SEQUENCES = new Metric.Builder("OEDB_NUM_SEQUENCES", "DB sequences",
      Metric.ValueType.INT) //
        .setDescription("Number of sequences") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE_DB) //
        .create();

  public static final Metric<Integer> NUM_INDEXES = new Metric.Builder("OEDB_NUM_INDEXES", "DB indexes",
      Metric.ValueType.INT) //
        .setDescription("Number of indexes") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE_DB) //
        .create();

  public static final Metric<Integer> NUM_FIELDS = new Metric.Builder("OEDB_NUM_FIELDS", "DB fields",
      Metric.ValueType.INT) //
        .setDescription("Number of fields") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE_DB) //
        .create();

  public static final Metric<Integer> NUM_TRIGGERS = new Metric.Builder("OEDB_NUM_TRIGGERS", "DB triggers",
      Metric.ValueType.INT) //
        .setDescription("Number of triggers") //
        .setDirection(Metric.DIRECTION_NONE) //
        .setQualitative(false) //
        .setDomain(DOMAIN_OPENEDGE_DB) //
        .create();

  // *****************************
  // CoreMetrics.DOMAIN_COMPLEXITY
  // *****************************

  public static final Metric<Integer> COMPLEXITY = new Metric.Builder("OE_COMPLEXITY",
      "Complexity (w/ include files content)", Metric.ValueType.INT) //
        .setDescription("Complexity (w/ include files content)") //
        .setDirection(Metric.DIRECTION_WORST) //
        .setQualitative(false) //
        .setDomain(CoreMetrics.DOMAIN_COMPLEXITY) //
        .create();

  @SuppressWarnings("rawtypes")
  private static final List<Metric> METRICS = ImmutableList.<Metric> builder().add(PACKAGES, CLASSES, PROCEDURES,
      INCLUDES, WINDOWS, NUM_TRANSACTIONS, SHR_DS, SHR_TT, SHR_VAR, NUM_TABLES, NUM_SEQUENCES, NUM_INDEXES,
      NUM_FIELDS, NUM_TRIGGERS, INTERNAL_PROCEDURES, INTERNAL_FUNCTIONS, METHODS, COMPLEXITY, DIRECTIVES).build();

  @SuppressWarnings("rawtypes")
  @Override
  public List<Metric> getMetrics() {
    return METRICS;
  }

}
