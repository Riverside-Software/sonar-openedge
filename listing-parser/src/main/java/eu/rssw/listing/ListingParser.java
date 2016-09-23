package eu.rssw.listing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.primitives.Ints;

public class ListingParser {
  private static final Logger LOG = LoggerFactory.getLogger(ListingParser.class);

  private final List<CodeBlock> blocks = new ArrayList<>();

  /**
   * Ctor
   * 
   * @param file File name shouldn't containn any space character
   * @throws IOException
   */
  public ListingParser(File file) throws IOException {
    if (file.getAbsolutePath().indexOf(' ') != -1) {
      throw new IllegalArgumentException("File name shouldn't contain space character");
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      parseFile(reader);
    }
  }

  public ListingParser(BufferedReader reader) throws IOException {
    parseFile(reader);
  }

  public Collection<CodeBlock> getBlocks() {
    return blocks;
  }

  public Collection<CodeBlock> getTransactionBlocks() {
    return Collections2.filter(blocks, new Predicate<CodeBlock>() {
      @Override
      public boolean apply(CodeBlock input) {
        return input.isTransaction();
      }
    });
  }

  public Collection<CodeBlock> getBlocksWithBuffer() {
    return Collections2.filter(blocks, new Predicate<CodeBlock>() {
      @Override
      public boolean apply(CodeBlock input) {
        return input.getBuffers() != null;
      }
    });
  }

  public CodeBlock getMainBlock() {
    Iterator<CodeBlock> iter = Collections2.filter(blocks, new Predicate<CodeBlock>() {
      @Override
      public boolean apply(CodeBlock input) {
        return input.getLineNumber() == 0;
      }
    }).iterator();
    if (iter.hasNext())
      return iter.next();

    return null;
  }

  private void parseFile(BufferedReader reader) throws IOException {
    boolean sourceDone = false;
    boolean newPage = true;
    boolean frames = false;
    String str;
    while ((str = reader.readLine()) != null) {
      if (str.length() == 0)
        continue;

      // All new pages start with 0xFF except first one
      newPage |= str.charAt(0) == '\f';

      if (newPage) {
        newPage = false;

        if (sourceDone) {
          // New page and source is already parsed, then we skip header
          reader.readLine();
          reader.readLine();
          reader.readLine();
        } else {
          // New page but source is not yet fully parsed, we verify if this page is still about source code
          reader.readLine();
          String str2 = reader.readLine();
          reader.readLine();
          if ((str2 != null) && !str2.startsWith("{} Line Blk")) {
            // Entering blocks section
            sourceDone = true;
          }
        }
      } else if (sourceDone) {
        if (str.charAt(0) == ' ') {
          if ("frames:".equalsIgnoreCase(str.substring(0, 12).trim()))
            frames = true;
          // Buffer or frame block
          if (frames)
            blocks.get(blocks.size() - 1).appendFrame(str.substring(13));
          else
            blocks.get(blocks.size() - 1).appendBuffer(str.substring(13));
        } else {
          List<String> splitter = Splitter.on(' ').trimResults().omitEmptyStrings().limit(5).splitToList(str);
          if (splitter.size() < 4)
            return;
          Integer lineNumber = Ints.tryParse(splitter.get(1));
          BlockType type = BlockType.getBlockType(splitter.get(2).toUpperCase());
          boolean transaction = "Yes".equals(splitter.get(3));
          frames = false;
          if (type != null) {
            blocks.add(new CodeBlock(type, lineNumber == null ? -1 : lineNumber, transaction, (splitter.size() == 5 ? splitter.get(4) : "")));
          } else {
            LOG.error("Unknown block type {}", splitter.get(2).toUpperCase());
          }
        }
      }
    }
  }
}
