package org.sonar.plugins.openedge.api.objects;

import java.util.Map;

import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.Schema;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eu.rssw.antlr.database.objects.DatabaseDescription;

public class KryoSerializers {

  private KryoSerializers() {
    // No-op
  }

  public static void addSerializers(Kryo kryo) {
    kryo.register(Schema.class, new SchemaSerializer(), 77);
    kryo.register(DatabaseWrapper.class, new DatabaseWrapperSerializer(), 78);
  }

  public static class SchemaSerializer extends Serializer<Schema> {

    @Override
    public void write(Kryo kryo, Output output, Schema object) {
      output.writeInt(object.getDatabases().size(), true);
      for (IDatabase db : object.getDatabases())  {
        kryo.writeClassAndObject(output, db);
      }
      kryo.writeClassAndObject(output, object.getAliases());
    }

    @Override
    public Schema read(Kryo kryo, Input input, Class<? extends Schema> type) {
      int len = input.readInt(true);
      IDatabase[] db = new IDatabase[len];
      for (int zz = 0; zz < len; zz++) {
        db[zz] = (IDatabase) kryo.readClassAndObject(input);
      }
      @SuppressWarnings({"unchecked", "rawtypes"})
      Map<String, String> aliases = (Map) kryo.readClassAndObject(input);
      Schema sch = new Schema(db);
      aliases.entrySet().forEach(it -> sch.createAlias(it.getKey(), it.getValue()));

      return sch;
    }
  }

  public static class DatabaseWrapperSerializer extends Serializer<DatabaseWrapper> {

    @Override
    public void write(Kryo kryo, Output output, DatabaseWrapper object) {
      kryo.writeClassAndObject(output, object.getDbDesc());
    }

    @Override
    public DatabaseWrapper read(Kryo kryo, Input input, Class<? extends DatabaseWrapper> type) {
      return new DatabaseWrapper((DatabaseDescription) kryo.readClassAndObject(input));
    }

  }
}
