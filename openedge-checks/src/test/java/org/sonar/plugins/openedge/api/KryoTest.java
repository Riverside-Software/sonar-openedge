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
package org.sonar.plugins.openedge.api;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import org.prorefactor.core.schema.Schema;
import org.sonar.plugins.openedge.api.objects.DatabaseWrapper;
import org.sonar.plugins.openedge.api.objects.KryoSerializers;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.pct.mapping.OpenEdgeVersion;

public class KryoTest {

  @Test
  public void testKryoSerialization() throws IOException {
    var desc = DumpFileUtils.getDatabaseDescription(Path.of("src/test/resources/sp2k.df"));
    var sch1 = new Schema(new DatabaseWrapper(desc, OpenEdgeVersion.V128));
    var kryo = getKryoInstance();
    var outputPath = Path.of("target/kryo/test01.bin");
    Files.createDirectories(outputPath.getParent());
    try (var out1 = Files.newOutputStream(outputPath); //
        Output input = new Output(out1)) {
      kryo.writeClassAndObject(input, sch1);
    }
    assertTrue(Files.exists(outputPath));
    assertTrue(Files.size(outputPath) > 1000);
  }

  private static Kryo getKryoInstance() {
    var kryo = new Kryo();
    kryo.setReferences(true);
    kryo.register(HashMap.class);
    kryo.register(ArrayList.class);
    kryo.register(EnumSet.class);
    eu.rssw.antlr.database.objects.KryoSerializers.addSerializers(kryo);
    KryoSerializers.addSerializers(kryo);

    return kryo;
  }
}
