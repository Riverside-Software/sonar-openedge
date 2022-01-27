/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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

  }

  public static CrossReference parseXREF(Path xref) {
    CrossReference doc = EMPTY_XREF;
    if ((xref != null) && Files.exists(xref)) {
      try (InputStream inpStream = Files.newInputStream(xref)) {
        doc = (CrossReference) UNMARSHALLER.unmarshal(new SAXSource(SAX_PARSER_FACTORY.newSAXParser().getXMLReader(),
            new InputSource(new InvalidXMLFilterStream(inpStream))));
      } catch (JAXBException | SAXException | ParserConfigurationException | IOException caught) {
        LOGGER.error("Unable to parse XREF file " + xref.toAbsolutePath(), caught);
      }
    }

    return doc;
  }

  public static CrossReference parseXREF(File xref) {
    return ((xref != null) && xref.exists()) ? parseXREF(xref.toPath()) : EMPTY_XREF;
  }

}
