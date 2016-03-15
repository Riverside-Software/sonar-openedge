/*
 * OpenEdge plugin for SonarQube
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
package org.sonar.plugins.openedge.foundation;

import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.google.common.collect.ImmutableList;

public class OpenEdgeMetrics implements Metrics {
  private final static String DOMAIN_OPENEDGE = "OpenEdge";

  public static final String CLASSES_KEY = "OE_CLASSES";
  public static final Metric<Integer> CLASSES = new Metric.Builder(CLASSES_KEY, "Classes",
      Metric.ValueType.INT).setDescription("Number of classes").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String PACKAGES_KEY = "OE_PACKAGES";
  public static final Metric<Integer> PACKAGES = new Metric.Builder(PACKAGES_KEY, "Packages",
      Metric.ValueType.INT).setDescription("Number of packages").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String PROCEDURES_KEY = "PROCEDURES";
  public static final Metric<Integer> PROCEDURES = new Metric.Builder(PROCEDURES_KEY, "Procedures",
      Metric.ValueType.INT).setDescription("Number of procedures").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String INCLUDES_KEY = "INCLUDES";
  public static final Metric<Integer> INCLUDES = new Metric.Builder(INCLUDES_KEY, "Includes",
      Metric.ValueType.INT).setDescription("Number of includes files").setDirection(
          Metric.DIRECTION_NONE).setQualitative(false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String WINDOWS_KEY = "WINDOWS";
  public static final Metric<Integer> WINDOWS = new Metric.Builder(WINDOWS_KEY, "Windows",
      Metric.ValueType.INT).setDescription("Number of GUI components").setDirection(
          Metric.DIRECTION_NONE).setQualitative(false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String DEBUG_LISTING_LOC_KEY = "DBG_LOC";
  public static final Metric<Integer> DEBUG_LISTING_LOC = new Metric.Builder(DEBUG_LISTING_LOC_KEY, "Debug listing LOC",
      Metric.ValueType.INT).setDescription("LOC in debug listing").setDirection(Metric.DIRECTION_NONE).setQualitative(
          false).create();

  public static final String DEBUG_LISTING_NCLOC_KEY = "DBG_NCLOC";
  public static final Metric<Integer> DEBUG_LISTING_NCLOC = new Metric.Builder(DEBUG_LISTING_NCLOC_KEY,
      "Debug listing NCLOC", Metric.ValueType.INT).setDescription("NCLOC in debug listing").setDirection(
          Metric.DIRECTION_NONE).setQualitative(false).create();

  public static final String DEBUG_LISTING_COMMENT_LINES_KEY = "DBG_COMMENT_LINES";
  public static final Metric<Integer> DEBUG_LISTING_COMMENT_LINES = new Metric.Builder(DEBUG_LISTING_COMMENT_LINES_KEY,
      "Debug listing comment lines", Metric.ValueType.INT).setDescription(
          "Comment lines in debug listing").setDirection(Metric.DIRECTION_NONE).setQualitative(false).create();

  public static final String DEBUG_LISTING_COMMENT_LINES_PERCENTAGE_KEY = "DBG_COMMENT_LINES_PERCENTAGE";
  public static final Metric<Double> DEBUG_LISTING_COMMENT_LINES_PERCENTAGE = new Metric.Builder(
      DEBUG_LISTING_COMMENT_LINES_PERCENTAGE_KEY, "Debug listing comment lines",
      Metric.ValueType.PERCENT).setDescription("% comment lines in debug listing").setDirection(
          Metric.DIRECTION_NONE).setQualitative(false).create();

  public static final String TRANSACTIONS_KEY = "TRANSACTIONS";
  public static final Metric<Integer> TRANSACTIONS = new Metric.Builder(TRANSACTIONS_KEY,
      "Transaction blocks line numbers", Metric.ValueType.DATA).setDescription(
          "Line numbers of transaction blocks").setDirection(Metric.DIRECTION_NONE).setQualitative(false).create();

  public static final String SHR_TT_KEY = "OE_SHR_TT";
  public static final Metric<Integer> SHR_TT = new Metric.Builder(SHR_TT_KEY, "New shared temp-tables",
      Metric.ValueType.INT).setDescription("Number of new shared temp-tables").setDirection(
          Metric.DIRECTION_NONE).setQualitative(false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String SHR_DS_KEY = "OE_SHR_DS";
  public static final Metric<Integer> SHR_DS = new Metric.Builder(SHR_DS_KEY, "New shared datasets",
      Metric.ValueType.INT).setDescription("Number of new shared datasets").setDirection(
          Metric.DIRECTION_NONE).setQualitative(false).setDomain(DOMAIN_OPENEDGE).create();

  public static final String SHR_VAR_KEY = "OE_SHR_VAR";
  public static final Metric<Integer> SHR_VAR = new Metric.Builder(SHR_VAR_KEY, "New shared variables",
      Metric.ValueType.INT).setDescription("Number of new shared variables").setDirection(
          Metric.DIRECTION_NONE).setQualitative(false).setDomain(DOMAIN_OPENEDGE).create();

  private static final List<Metric> METRICS = ImmutableList.<Metric> builder().add(PACKAGES, CLASSES, PROCEDURES, INCLUDES, WINDOWS, TRANSACTIONS, SHR_DS, SHR_TT, SHR_VAR).build();

  @Override
  public List<Metric> getMetrics() {
    return METRICS;
  }

}
