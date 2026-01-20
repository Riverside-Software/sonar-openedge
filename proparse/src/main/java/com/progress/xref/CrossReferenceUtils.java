/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package com.progress.xref;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.progress.xref.CrossReference.Source;
import com.progress.xref.CrossReference.Source.Reference;

public class CrossReferenceUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrossReferenceUtils.class);
  private static final SAXParserFactory SAX_PARSER_FACTORY;
  private static final JAXBContext CONTEXT;
  private static final Unmarshaller UNMARSHALLER;
  private static final CrossReference EMPTY_XREF = new EmptyCrossReference();

  static {
    SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
    SAX_PARSER_FACTORY.setNamespaceAware(false);
    try {
      SAX_PARSER_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      CONTEXT = JAXBContext.newInstance("com.progress.xref", CrossReference.class.getClassLoader());
      UNMARSHALLER = CONTEXT.createUnmarshaller();
    } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException
        | JAXBException caught) {
      throw new IllegalStateException(caught);
    }
  }

  private CrossReferenceUtils() {
    // No-op
  }

  public static CrossReference parseXREF(File xref) {
    if (xref != null)
      LOGGER.debug("XREF file: {} - Found: {}", xref.getAbsolutePath(), xref.exists());
    else
      LOGGER.debug("Null XREF reference");
    return ((xref != null) && xref.exists()) ? parseXREF(xref.toPath()) : EMPTY_XREF;
  }

  public static CrossReference parseXREF(Path xref) {
    CrossReference doc = EMPTY_XREF;
    if ((xref != null) && Files.exists(xref)) {
      // Detect if file is plain text or XML
      boolean xml = false;
      String plainTextCodepage = null;
      try (InputStream inpStream = Files.newInputStream(xref)) {
        // Start with <?xml
        xml = (inpStream.read() == 0x3c) && (inpStream.read() == 0x3f) && (inpStream.read() == 0x78)
            && (inpStream.read() == 0x6d) && (inpStream.read() == 0x6c);
        if (!xml) {
          // Read third line of plain text XREF (search CPSTREAM line)
          try (InputStreamReader r1 = new InputStreamReader(inpStream); BufferedReader r2 = new BufferedReader(r1)) {
            String line = r2.readLine();
            line = r2.readLine();
            line = r2.readLine();
            if (line == null)
              return doc;
            int pos = line.lastIndexOf(' ');
            if (pos == -1)
              return doc;
            plainTextCodepage = line.substring(pos + 1);
          }
        }
      } catch (IOException caught) {
        LOGGER.error("Unable to open file " + xref.toAbsolutePath(), caught);
        return doc;
      }

      try (InputStream inpStream = Files.newInputStream(xref)) {
        if (xml)
          return parseXmlXref(inpStream);
        else
          return parsePlainTextXref(xref.toAbsolutePath().toString(), inpStream, getCharset(plainTextCodepage));
      } catch (IOException caught) {
        LOGGER.error("Unable to open file " + xref.toAbsolutePath(), caught);
      }
    }
    return doc;
  }

  private static CrossReference parseXmlXref(InputStream input) throws IOException {
    CrossReference doc = EMPTY_XREF;
    try {
      doc = (CrossReference) UNMARSHALLER.unmarshal(new SAXSource(SAX_PARSER_FACTORY.newSAXParser().getXMLReader(),
          new InputSource(new InvalidXMLFilterStream(input))));
    } catch (JAXBException | SAXException | ParserConfigurationException caught) {
      throw new IOException(caught);
    }

    return doc;
  }

  private static void handleIncludeReference(Source src, Reference ref) {
    // TODO Formatted for IMPORT -> Remove double quotes
    src.reference.add(ref);
  }

  private static void handleRunReference(Source src, Reference ref) {
    if (ref.objectIdentifier.endsWith(" PERSISTENT") //
        || ref.objectIdentifier.endsWith(" REMOTE") //
        || ref.objectIdentifier.endsWith(" SINGLE-RUN") //
        || ref.objectIdentifier.endsWith(" SINGLETON") //
        || ref.objectIdentifier.endsWith(" SUPER") //
        || ref.objectIdentifier.endsWith(" STORED-PROC")) {
      int pos = ref.objectIdentifier.lastIndexOf(' ');
      ref.objectContext = ref.objectIdentifier.substring(pos + 1);
      ref.objectIdentifier = ref.objectIdentifier.substring(0, pos);
    }
    src.reference.add(ref);
  }

  private static void handleSortAccessReference(Source src, Reference ref) {
    if (ref.objectIdentifier.endsWith(" WORKFILE") || ref.objectIdentifier.endsWith(" TEMPTABLE")) {
      int pos = ref.objectIdentifier.lastIndexOf(' ');
      ref.tempRef = "T";
      ref.objectIdentifier = ref.objectIdentifier.substring(0, pos);
    }
    int pos = ref.objectIdentifier.lastIndexOf(' ');
    ref.objectContext = ref.objectIdentifier.substring(pos + 1);
    ref.objectIdentifier = ref.objectIdentifier.substring(0, pos);
    src.reference.add(ref);
  }

  private static void handleSearchReference(Source src, Reference ref) {
    String part1 = ref.objectIdentifier.substring(0, ref.objectIdentifier.indexOf(' '));
    if ("DATA-MEMBER".equalsIgnoreCase(part1) || "INHERITED-DATA-MEMBER".equalsIgnoreCase(part1)
        || (part1.indexOf(':') != -1)) {
      ref.objectIdentifier = ref.objectIdentifier.substring(ref.objectIdentifier.indexOf(':') + 1);
    }
    if (ref.objectIdentifier.endsWith("WORKFILE")) {
      ref.tempRef = "T";
      ref.objectIdentifier = part1;
    } else {
      if (ref.objectIdentifier.endsWith(" WHOLE-INDEX") || ref.objectIdentifier.endsWith(" TABLE-SCAN")) {
        int pos = ref.objectIdentifier.lastIndexOf(' ');
        ref.detail = ref.objectIdentifier.substring(pos + 1);
        ref.objectIdentifier = ref.objectIdentifier.substring(0, pos);
      }
      if (ref.objectIdentifier.endsWith(" TEMPTABLE")) {
        int pos = ref.objectIdentifier.lastIndexOf(' ');
        ref.tempRef = "T";
        ref.objectIdentifier = ref.objectIdentifier.substring(0, pos);
      }
      int pos = ref.objectIdentifier.lastIndexOf(' ');
      ref.objectContext = ref.objectIdentifier.substring(pos + 1);
      ref.objectIdentifier = ref.objectIdentifier.substring(0, pos);
    }
    src.reference.add(ref);
  }

  private static void handleInvokeReference(Source src, Reference ref) {
    int pos = ref.objectIdentifier.indexOf(',');
    ref.objectIdentifier = pos == -1 ? ref.objectIdentifier : ref.objectIdentifier.substring(0, pos);
    src.reference.add(ref);
  }

  private static void handleNewSharedReference(Source src, Reference ref) {
    int pos = ref.objectIdentifier.indexOf(',');
    ref.objectIdentifier = pos == -1 ? ref.objectIdentifier : ref.objectIdentifier.substring(0, pos);
    src.reference.add(ref);
  }

  private static CrossReference parsePlainTextXref(String fName, InputStream input, Charset charset) throws IOException {
    CrossReference doc = new CrossReference();
    doc.source = new ArrayList<>();

    Map<String, Source> sources = new HashMap<>();
    try (InputStream inpStream = new InvalidXMLFilterStream(input);
        InputStreamReader reader = new InputStreamReader(inpStream, charset);
        BufferedReader reader2 = new BufferedReader(reader)) {
      Source currSrc = null;
      String currSrcStr = null;
      String ln = null;
      int lineNum = 0;
      final AtomicInteger fileNum = new AtomicInteger(1);
      final AtomicInteger seqNum = new AtomicInteger(1);
      while ((ln = reader2.readLine()) != null) {
        lineNum++;
        try {
          String[] line = parseLine(ln);
          if ((currSrc == null) || (currSrcStr == null) || !currSrcStr.equals(line[1])) {
            currSrcStr = line[1];
            currSrc = sources.computeIfAbsent(line[1], key -> {
              Source src = new Source();
              src.fileNum = fileNum.getAndIncrement();
              src.fileName = key;
              src.reference = new ArrayList<>();
              doc.source.add(src);
              return src;
            });
          }
  
          Reference ref = new Reference();
          ref.referenceType = line[3];
          ref.fileNum = currSrc.fileNum;
          ref.refSeq = seqNum.getAndIncrement();
          // Line-number can be 'IMPLICIT' or 'NONE'
          ref.lineNum = !Character.isDigit(line[2].charAt(0)) ? 0 : Integer.parseInt(line[2]);
          ref.objectIdentifier = (line[4].length() > 2) && (line[4].charAt(0) == '"')
              && (line[4].charAt(line[4].length() - 1) == '"') ? line[4].substring(1, line[4].length() - 1) : line[4];
  
          switch (line[3]) {
            case "INCLUDE":
              handleIncludeReference(currSrc, ref);
              break;
            case "RUN":
              handleRunReference(currSrc, ref);
              break;
            case "SORT-ACCESS":
              handleSortAccessReference(currSrc, ref);
              break;
            case "SEARCH":
              handleSearchReference(currSrc, ref);
              break;
            case "INVOKE":
            case "NEW":
              handleInvokeReference(currSrc, ref);
              break;
            case "NEW-SHR-TEMPTABLE":
            case "NEW-SHR-DATASET":
            case "NEW-SHR-VARIABLE":
              handleNewSharedReference(currSrc, ref);
              break;
          }
        } catch (Exception caught) {
          LOGGER.debug("File '{}' - Unable to parse XREF line {}: '{}' -- Msg: {}", fName, lineNum, ln,
              caught.getMessage());
        }
      }
    }

    return doc;
  }

  private static String[] parseLine(String line) {
    String[] retVal = {"", "", "", "", ""};
    int currPos = 0;
    if (line.charAt(currPos) == '"') {
      int endIndex = line.indexOf('"', 1);
      retVal[0] = line.substring(1, endIndex);
      currPos = endIndex + 2;
    } else {
      int endIndex = line.indexOf(' ', 1);
      retVal[0] = line.substring(0, endIndex);
      currPos = endIndex + 1;
    }

    if (line.charAt(currPos) == '"') {
      int endIndex = line.indexOf('"', currPos + 1);
      retVal[1] = line.substring(currPos + 1, endIndex);
      currPos = endIndex + 2;
    } else {
      int endIndex = line.indexOf(' ', currPos + 1);
      retVal[1] = line.substring(currPos, endIndex);
      currPos = endIndex + 1;
    }

    retVal[2] = line.substring(currPos, line.indexOf(' ', currPos));
    currPos = line.indexOf(' ', currPos) + 1;
    retVal[3] = line.substring(currPos, line.indexOf(' ', currPos));
    currPos = line.indexOf(' ', currPos) + 1;
    retVal[4] = line.substring(currPos);

    return retVal;
  }

  /**
   * Return charset to be used when writing files in Java to be read by Progress session (thus according to cpstream,
   * parameter files, ...) and dealing with OE encodings (such as undefined or 1252). Imported from PCT
   */
  private static Charset getCharset(String charset) {
    if (charset == null) {
      return StandardCharsets.UTF_8;
    }

    try {
      if ("1250".equals(charset))
        return Charset.forName("windows-1250");
      else if ("1251".equals(charset))
        return Charset.forName("windows-1251");
      else if ("1252".equals(charset))
        return Charset.forName("windows-1252");
      else if ("1253".equals(charset))
        return Charset.forName("windows-1253");
      else if ("1254".equals(charset))
        return Charset.forName("windows-1254");
      else if ("1255".equals(charset))
        return Charset.forName("windows-1255");
      else if ("1256".equals(charset))
        return Charset.forName("windows-1256");
      else if ("1257".equals(charset))
        return Charset.forName("windows-1257");
      else if ("1258".equals(charset))
        return Charset.forName("windows-1258");
      else if ("big-5".equalsIgnoreCase(charset))
        return Charset.forName("Big5");
      else
        return Charset.forName(charset);
    } catch (IllegalArgumentException caught) {
      return StandardCharsets.UTF_8;
    }
  }

}
