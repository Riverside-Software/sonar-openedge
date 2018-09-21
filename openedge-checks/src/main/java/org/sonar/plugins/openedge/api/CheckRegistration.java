/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package org.sonar.plugins.openedge.api;

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.openedge.api.checks.OpenEdgeDumpFileCheck;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;

/**
 * Implement this interface to register Proparse or DumpFile checks
 */
@ServerSide
@ScannerSide
public interface CheckRegistration {

  void register(Registrar registrar);

  interface Registrar {
    public void registerParserCheck(Class<? extends OpenEdgeProparseCheck> check);

    public void registerDumpFileCheck(Class<? extends OpenEdgeDumpFileCheck> check);
  }

}
