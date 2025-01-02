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
package eu.rssw.listing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

public class ListingParser {
  private static final Logger LOG = LoggerFactory.getLogger(ListingParser.class);

  private final List<CodeBlock> blocks = new ArrayList<>();
  private final String relativeName;

  /**
   * Ctor
   * 
   * @param path File name shouldn't contain any space character
   * @throws IOException
   */
  public ListingParser(Path path, String relativeName) throws IOException {
    if (path.toAbsolutePath().toString().indexOf(' ') != -1) {
      throw new IllegalArgumentException(
          "File name shouldn't contain space character - '" + path.toAbsolutePath().toString() + "'");
    }
    this.relativeName = relativeName;
    // Force iso8859-1 encoding so that all crappy characters inserted in the listing will be processed without an exception
    // We only care about the last part of the file, which doesn't contain non-ASCII characters
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
      parseFile(reader);
    }
  }

  public ListingParser(BufferedReader reader, String relativeName) throws IOException {
    this.relativeName = relativeName;
    parseFile(reader);
  }

  public Collection<CodeBlock> getBlocks() {
    return blocks;
  }

  public Collection<CodeBlock> getTransactionBlocks() {
    return blocks.stream().filter(CodeBlock::isTransaction).collect(Collectors.toList());
  }

  public Collection<CodeBlock> getBlocksWithBuffer() {
    return blocks.stream().filter(it -> it.getBuffers() != null).collect(Collectors.toList());
  }

  public CodeBlock getMainBlock() {
    return blocks.stream().filter(it -> it.getLineNumber() == 0).findFirst().orElse(null);
  }

  private void parseFile(BufferedReader reader) throws IOException {
    boolean sourceDone = false;
    boolean newPage = true;
    boolean frames = false;
    String str;
    int fileLineNumber = 0;
    while ((str = reader.readLine()) != null) {
      fileLineNumber++;
      if (str.length() == 0)
        continue;

      // All new pages start with 0xFF except first one
      newPage |= str.charAt(0) == '\f';

      if (newPage) {
        newPage = false;

        if (sourceDone) {
          // New page and source is already parsed, then we skip header
          String str1 = reader.readLine();
          String str2 = reader.readLine();
          String str3 = reader.readLine();
          if ((str1 == null) || (str2 == null) || (str3 == null))
            return;
        } else {
          // New page but source is not yet fully parsed, we verify if this page is still about source code
          String str1 = reader.readLine();
          String str2 = reader.readLine();
          String str3 = reader.readLine();
          if ((str1 != null) && (str2 != null) && (str3 != null) && !str2.startsWith("{} Line Blk")) {
            // Entering blocks section
            sourceDone = true;
          }
        }
        fileLineNumber += 3;
      } else if (sourceDone) {
        if (str.charAt(0) == ' ') {
          if ("frames:".equalsIgnoreCase(str.substring(0, 12).trim()))
            frames = true;
          // Buffer or frame block
          if (blocks.isEmpty()) {
            // Caused by unknown block type with associated buffers
            LOG.error("No matching block in {} at line {}, please report issue", relativeName, fileLineNumber);
          } else {
            if (frames)
              blocks.get(blocks.size() - 1).appendFrame(str.substring(13));
            else
              blocks.get(blocks.size() - 1).appendBuffer(str.substring(13));
          }
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
            LOG.error("Unknown block type {} in {} at line {}, please report issue", splitter.get(2).toUpperCase(), relativeName, fileLineNumber);
          }
        }
      }
    }
  }
}
