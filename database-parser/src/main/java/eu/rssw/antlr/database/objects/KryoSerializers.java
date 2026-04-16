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
package eu.rssw.antlr.database.objects;

import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

public class KryoSerializers {

  private KryoSerializers() {
    // No-op
  }

  public static void addSerializers(Kryo kryo) {
    kryo.register(DatabaseDescription.class, 70);
    kryo.register(Table.class, new DbTableSerializer(), 71);
    kryo.register(Field.class, new DbFieldSerializer(), 72);
    kryo.register(Index.class, new DbIndexSerializer(), 73);
    kryo.register(Sequence.class, new DbSequenceSerializer(), 74);
    kryo.register(Trigger.class, new DbTriggerSerializer(), 75);
    kryo.register(TriggerType.class, 76);
  }

  public static class DbTableSerializer extends Serializer<Table> {
    @Override
    public void write(Kryo kryo, Output output, Table table) {
      output.writeString(table.getName());
      output.writeString(table.getArea());
      output.writeString(table.getDescription());
      output.writeString(table.getDumpName());

      var ser = new CollectionSerializer<>();
      //ser.setAcceptsNull(true);
      //ser.setElementClass(Field.class, new DbFieldSerializer());
      //ser.setElementsCanBeNull(false);
      
      kryo.writeObject(output, table.getFields(), ser);
      /*var fields = new ArrayList<>(table.getFields());
      output.writeInt(fields.size(), true);
      for (Field f : fields) {
        kryo.writeObject(output, f);
      }*/

      output.writeVarInt(table.getFirstLine(), false);
      output.writeBoolean(table.isFrozen());

      kryo.writeObject(output, table.getIndexes(), new CollectionSerializer<>());
      /*var indexes = new ArrayList<>(table.getIndexes());
      output.writeInt(indexes.size(), true);
      for (Index idx : indexes) {
        kryo.writeObject(output, idx);
      }*/

      
      output.writeString(table.getLabel());
      output.writeVarInt(table.getLastLine(), false);
      
      kryo.writeObject(output, table.getTriggers(), new CollectionSerializer<>());
      
      /*var triggers = new ArrayList<>(table.getTriggers());
      output.writeInt(triggers.size(), true);
      for (Trigger t : triggers) {
        kryo.writeObject(output, t);
      }*/
      
      output.writeString(table.getValMsg());
      



    }

    @Override
    public Table read(Kryo kryo, Input input, Class<? extends Table> type) {
      Table.Builder builder = new Table.Builder(input.readString())
          .setArea(input.readString())
          .setLabel(input.readString())
          .setDescription(input.readString())
          .setDumpName(input.readString())
          .setValMsg(input.readString())
          .setFrozen(input.readBoolean())
          .setFirstLine(input.readInt())
          .setLastLine(input.readInt());

      int fieldCount = input.readInt(true);
      for (int i = 0; i < fieldCount; i++) {
        builder.addField(kryo.readObject(input, Field.class));
      }

      int indexCount = input.readInt(true);
      for (int i = 0; i < indexCount; i++) {
        Index rawIdx = kryo.readObject(input, Index.class);
        // Rebuild index with correct field references from this table
        Index.Builder idxBuilder = new Index.Builder(rawIdx.getName())
            .setArea(rawIdx.getArea())
            .setPrimary(rawIdx.isPrimary())
            .setUnique(rawIdx.isUnique())
            .setWord(rawIdx.isWord())
            .setBufferPool(rawIdx.getBufferPool())
            .setFirstLine(rawIdx.getFirstLine())
            .setLastLine(rawIdx.getLastLine());
        for (IndexField fld : rawIdx.getFields()) {
          idxBuilder.addField(new IndexField(builder.getField(fld.getField().getName()), fld.isAscending()));
        }
        builder.addIndex(idxBuilder.build());
      }

      int triggerCount = input.readInt(true);
      for (int i = 0; i < triggerCount; i++) {
        builder.addTrigger(kryo.readObject(input, Trigger.class));
      }

      return builder.build();
    }
  }

  public static class DbFieldSerializer extends Serializer<Field> {
    @Override
    public void write(Kryo kryo, Output output, Field field) {
      output.writeString(field.getName());
      output.writeString(field.getDataType());
      output.writeString(field.getColumnLabel());
      output.writeString(field.getDescription());
      kryo.writeObjectOrNull(output, field.getExtent(), Integer.class);
      output.writeVarInt(field.getFirstLine(),false);
      output.writeString(field.getFormat());
      output.writeString(field.getInitial());
      output.writeBoolean(field.isMandatory());
      output.writeString(field.getLabel());
      output.writeVarInt(field.getLastLine(), false);
      output.writeString(field.getLobArea());
      kryo.writeObjectOrNull(output, field.getMaxWidth(), Integer.class);
      kryo.writeObjectOrNull(output, field.getOrder(), Integer.class);
      kryo.writeObjectOrNull(output, field.getPosition(), Integer.class);

      kryo.writeObject(output, field.getTriggers(), new CollectionSerializer<>());
      /*var triggers = new ArrayList<>(field.getTriggers());
      output.writeInt(triggers.size(), true);
      for (Trigger t : triggers) {
        kryo.writeObject(output, t);
      }*/
    }

    @Override
    public Field read(Kryo kryo, Input input, Class<? extends Field> type) {
      Field.Builder builder = new Field.Builder(input.readString(), input.readString())
          .setOrder(kryo.readObjectOrNull(input, Integer.class))
          .setPosition(kryo.readObjectOrNull(input, Integer.class))
          .setExtent(kryo.readObjectOrNull(input, Integer.class))
          .setDescription(input.readString())
          .setLabel(input.readString())
          .setColumnLabel(input.readString())
          .setLobArea(input.readString())
          .setFormat(input.readString())
          .setInitial(input.readString())
          .setMaxWidth(kryo.readObjectOrNull(input, Integer.class))
          .setMandatory(input.readBoolean())
          .setFirstLine(input.readInt())
          .setLastLine(input.readInt());

      int triggerCount = input.readInt(true);
      for (int i = 0; i < triggerCount; i++) {
        builder.addTrigger(kryo.readObject(input, Trigger.class));
      }

      return builder.build();
    }
  }

  public static class DbIndexSerializer extends Serializer<Index> {
    @Override
    public void write(Kryo kryo, Output output, Index index) {
      output.writeString(index.getName());

      output.writeInt(index.getFields().size(), true);
      for (IndexField idxFld : index.getFields()) {
        output.writeString(idxFld.getField().getName());
        output.writeBoolean(idxFld.isAscending());
      }

      output.writeString(index.getArea());
      output.writeString(index.getBufferPool());
      output.writeVarInt(index.getFirstLine(), false);
      output.writeVarInt(index.getLastLine(), false);
      output.writeBoolean(index.isPrimary());
      output.writeBoolean(index.isUnique());
      output.writeBoolean(index.isWord());

    }

    @Override
    public Index read(Kryo kryo, Input input, Class<? extends Index> type) {
      Index.Builder builder = new Index.Builder(input.readString())
          .setArea(input.readString())
          .setPrimary(input.readBoolean())
          .setUnique(input.readBoolean())
          .setWord(input.readBoolean())
          .setBufferPool(input.readString())
          .setFirstLine(input.readInt())
          .setLastLine(input.readInt());

      int fieldCount = input.readInt(true);
      for (int i = 0; i < fieldCount; i++) {
        builder.addField(new IndexField(
            new Field.Builder(input.readString(), "SER_TYPE").build(),
            input.readBoolean()));
      }

      return builder.build();
    }
  }

  public static class DbSequenceSerializer extends Serializer<Sequence> {
    @Override
    public void write(Kryo kryo, Output output, Sequence seq) {
      output.writeString(seq.getName());
      output.writeBoolean(seq.isCycleOnLimit());
      output.writeVarInt(seq.getFirstLine(), false);
      kryo.writeObjectOrNull(output, seq.getIncrement(), Long.class);
      kryo.writeObjectOrNull(output, seq.getInitialValue(), Long.class);
      output.writeVarInt(seq.getLastLine(), false);
      kryo.writeObjectOrNull(output, seq.getMaxValue(), Long.class);
      kryo.writeObjectOrNull(output, seq.getMinValue(), Long.class);
    }

    @Override
    public Sequence read(Kryo kryo, Input input, Class<? extends Sequence> type) {
      return new Sequence.Builder(input.readString())
          .setCycleOnLimit(input.readBoolean())
          .setFirstLine(input.readVarInt(false))
          .setIncrement(kryo.readObjectOrNull(input, Long.class))
          .setInitialValue(kryo.readObjectOrNull(input, Long.class))
          .setLastLine(input.readVarInt(false))
          .setMaxValue(kryo.readObjectOrNull(input, Long.class))
          .setMinValue(kryo.readObjectOrNull(input, Long.class))
          .build();
    }
  }

  public static class DbTriggerSerializer extends Serializer<Trigger> {
    @Override
    public void write(Kryo kryo, Output output, Trigger trigger) {
      kryo.writeClassAndObject(output, trigger.getType());
      output.writeString(trigger.getProcedure());
      output.writeString(trigger.getCrc());
      output.writeBoolean(trigger.isNoOverride());
      output.writeBoolean(trigger.isOverride());
    }

    @Override
    public Trigger read(Kryo kryo, Input input, Class<? extends Trigger> type) {
      return new Trigger.Builder((TriggerType) kryo.readClassAndObject(input), input.readString())
          .setNoOverride(input.readBoolean())
          .setOverride(input.readBoolean())
          .setCrc(input.readString())
          .build();
    }
  }

}
