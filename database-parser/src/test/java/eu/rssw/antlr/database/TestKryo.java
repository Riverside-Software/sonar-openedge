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
package eu.rssw.antlr.database;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.antlr.database.objects.Field;
import eu.rssw.antlr.database.objects.KryoSerializers;
import eu.rssw.antlr.database.objects.Table;

public class TestKryo {

  private Kryo getKryoInstance() {
    Kryo kryo = new Kryo();
    KryoSerializers.addSerializers(kryo);
    kryo.register(HashMap.class);
    kryo.register(ArrayList.class);

    return kryo;
  }

  @BeforeTest
  public void init() throws IOException {
    try {
      Files.createDirectories(Paths.get("target/kryo"));
    } catch (FileAlreadyExistsException caught) {
      // No-op
    }
  }

  @Test
  public void test01() throws IOException {
    DatabaseDescription db = DumpFileUtils.getDatabaseDescription(Paths.get("src/test/resources/sp2k.df"));
    assertEquals(db.getTables().size(), 25);
    assertEquals(db.getSequences().size(), 13);

    Kryo kryo = getKryoInstance();
    try (OutputStream fos = new FileOutputStream("target/kryo/test01.bin"); //
        Output output = new Output(fos)) {
      kryo.writeClassAndObject(output, db);
    }

    DatabaseDescription db2 = null;
    try (InputStream fis = new FileInputStream("target/kryo/test01.bin");
        Input input = new Input(fis)) {
      Object o = kryo.readClassAndObject(input);
      assertTrue(o instanceof DatabaseDescription);
      db2 = (DatabaseDescription) db;
    }

    assertEquals(db.getSequences().size(), db2.getSequences().size());
    assertEquals(db.getTables().size(), db2.getTables().size());
    Table tbl1 = db.getTable("Item");
    Table tbl2 = db2.getTable("Item");
    assertNotNull(tbl1);
    assertNotNull(tbl2);
    assertEquals(tbl1.getFields().size(), tbl2.getFields().size());
    assertEquals(tbl1.getIndexes().size(), tbl2.getIndexes().size());
    assertEquals(tbl1.getFields().iterator().next().getName(), tbl2.getFields().iterator().next().getName());
    assertEquals(tbl1.getIndexes().iterator().next().getName(), tbl2.getIndexes().iterator().next().getName());
    // Test that indexFields are references to real fields
    Field idxFld1 = tbl2.getIndex("ItemNum").getFields().get(0).getField();
    assertNotNull(idxFld1);
    assertTrue(tbl2.getFields().contains(idxFld1));
  }

}
