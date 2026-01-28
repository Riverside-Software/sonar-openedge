package org.prorefactor.core.schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.io.LineProcessor;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.mapping.OpenEdgeVersion;

public class MetaSchemaProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaSchemaProvider.class);

  private MetaSchemaProvider() {
    // Hide default constructor
  }

  public static List<ITable> getMetaSchema(IDatabase db, OpenEdgeVersion version) {
    var lineProcessor = new SchemaLineProcessor(db);
    try (var input = OpenEdgeVersion.class.getResourceAsStream(version.getMetaschema());
        var reader1 = new InputStreamReader(input);
        var reader = new BufferedReader(reader1)) {

      var line = reader.readLine();
      while (line != null) {
        lineProcessor.processLine(line);
        line = reader.readLine();
      }
    } catch (IOException caught) {
      LOGGER.error("Unable to open {}", version.getMetaschema(), caught);
    }

    return lineProcessor.getResult();
  }

  public static class SchemaLineProcessor implements LineProcessor<List<ITable>> {
    private final IDatabase currDatabase;
    private final List<ITable> tables = new ArrayList<>();
    private Table currTable;

    public SchemaLineProcessor(IDatabase currDatabase) {
      this.currDatabase = currDatabase;
    }

    @Override
    public boolean processLine(String line) throws IOException {
      if (line.startsWith("S")) {
        // No support for sequences
      } else if (line.startsWith("T")) {
        currTable = new Table(line.substring(1), currDatabase);
        tables.add(currTable);
      } else if (line.startsWith("F")) {
        // FieldName:DataType:Extent
        int ch1 = line.indexOf(':');
        int ch2 = line.lastIndexOf(':');
        if ((currTable == null) || (ch1 == -1) || (ch2 == -1))
          throw new IOException("Invalid file format: " + line);
        var f = new Field(line.substring(1, ch1), currTable);
        f.setDataType(DataType.get(line.substring(ch1 + 1, ch2)));
        f.setExtent(Integer.parseInt(line.substring(ch2 + 1)));
        currTable.add(f);
      } else if (line.startsWith("I")) {
        if (currTable == null)
          throw new IOException("No associated table for " + line);
        // IndexName:Attributes:Field1:Field2:...
        List<String> lst = Splitter.on(':').trimResults().splitToList(line);
        if (lst.size() < 3)
          throw new IOException("Invalid file format: " + line);
        var i = new Index(currTable, lst.get(0).substring(1), lst.get(1).indexOf('U') > -1, lst.get(1).indexOf('P') > -1);
        for (int zz = 2; zz < lst.size(); zz++) {
          i.addField(currTable.lookupField(lst.get(zz).substring(1)));
        }
        currTable.add(i);
      }

      return true;
    }

    @Override
    public List<ITable> getResult() {
      return tables;
    }
  }

}
