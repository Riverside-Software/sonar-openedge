/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
package eu.rssw.antlr.database.objects;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

public class KryoSerializers {

  private KryoSerializers() {
    // No-op
  }

  public static void addSerializers(Kryo kryo) {
    kryo.register(DatabaseDescription.class, 70);
    kryo.register(Table.class, new KryoSerializers.DbTableSerializer(kryo, Table.class), 71);
    kryo.register(Field.class, new KryoSerializers.DbFieldSerializer(kryo, Field.class), 72);
    kryo.register(Index.class, new KryoSerializers.DbIndexSerializer(kryo, Index.class), 73);
    kryo.register(Sequence.class, new KryoSerializers.DbSequenceSerializer(kryo, Sequence.class), 74);
    kryo.register(Trigger.class, new KryoSerializers.DbTriggerSerializer(kryo, Trigger.class), 75);
    kryo.register(TriggerType.class, 76);
  }

  public static class DbTableSerializer extends FieldSerializer<Table> {
    public DbTableSerializer(Kryo kryo, Class<Table> type) {
      super(kryo, type);
    }

    @Override
    protected void initializeCachedFields() {
      removeField("name");
    }

    @Override
    protected Table create(Kryo kryo, Input input, Class<? extends Table> type) {
      return new Table(input.readString());
    }

    @Override
    public void write(Kryo kryo, Output output, Table object) {
      // Final fields first
      output.writeString(object.getName());
      // Then standard fields
      super.write(kryo, output, object);
    }

    @Override
    public Table read(Kryo kryo, Input input, Class<? extends Table> type) {
      Table tbl = super.read(kryo, input, type);
      // Rewrite index fields
      for (Index idx : tbl.getIndexes()) {
        List<IndexField> newList = new ArrayList<>();
        for (IndexField fld : idx.getFields()) {
          newList.add(new IndexField(tbl.getField(fld.getField().getName()), fld.isAscending()));
        }
        idx.getFields().clear();
        idx.getFields().addAll(newList);
      }

      return tbl;
    }
  }

  public static class DbFieldSerializer extends FieldSerializer<Field> {
    public DbFieldSerializer(Kryo kryo, Class<Field> type) {
      super(kryo, type);
    }

    @Override
    protected void initializeCachedFields() {
      removeField("name");
      removeField("dataType");
    }

    @Override
    protected Field create(Kryo kryo, Input input, Class<? extends Field> type) {
      return new Field(input.readString(), input.readString());
    }

    @Override
    public void write(Kryo kryo, Output output, Field object) {
      // Final fields first
      output.writeString(object.getName());
      output.writeString(object.getDataType());
      // Then standard fields
      super.write(kryo, output, object);
    }
  }

  public static class DbIndexSerializer extends FieldSerializer<Index> {
    public DbIndexSerializer(Kryo kryo, Class<Index> type) {
      super(kryo, type);
    }

    @Override
    protected void initializeCachedFields() {
      removeField("name");
      removeField("fields");
    }

    @Override
    protected Index create(Kryo kryo, Input input, Class<? extends Index> type) {
      Index idx = new Index(input.readString());
      int len = input.readInt(true);
      for (int zz = 0; zz < len; zz++) {
        idx.addField(new IndexField(new Field(input.readString(), "SER_TYPE"), input.readBoolean()));
      }

      return idx;
    }

    @Override
    public void write(Kryo kryo, Output output, Index object) {
      // Final fields first
      output.writeString(object.getName());
      output.writeInt(object.getFields().size(), true);
      for (IndexField idxFld : object.getFields()) {
        output.writeString(idxFld.getField().getName());
        output.writeBoolean(idxFld.isAscending());
      }
      // Then standard fields
      super.write(kryo, output, object);
    }
  }

  public static class DbSequenceSerializer extends FieldSerializer<Sequence> {
    public DbSequenceSerializer(Kryo kryo, Class<Sequence> type) {
      super(kryo, type);
    }

    @Override
    protected void initializeCachedFields() {
      removeField("name");
    }

    @Override
    protected Sequence create(Kryo kryo, Input input, Class<? extends Sequence> type) {
      return new Sequence(input.readString());
    }

    @Override
    public void write(Kryo kryo, Output output, Sequence object) {
      // Final fields first
      output.writeString(object.getName());
      // Then standard fields
      super.write(kryo, output, object);
    }
  }

  public static class DbTriggerSerializer extends FieldSerializer<Trigger> {
    public DbTriggerSerializer(Kryo kryo, Class<Trigger> type) {
      super(kryo, type);
    }

    @Override
    protected void initializeCachedFields() {
      removeField("type");
      removeField("procedure");
    }

    @Override
    protected Trigger create(Kryo kryo, Input input, Class<? extends Trigger> type) {
      return new Trigger((TriggerType) kryo.readClassAndObject(input), input.readString());
    }

    @Override
    public void write(Kryo kryo, Output output, Trigger object) {
      // Final fields first
      kryo.writeClassAndObject(output, object.getType());
      output.writeString(object.getProcedure());
      // Then standard fields
      super.write(kryo, output, object);
    }
  }

}
