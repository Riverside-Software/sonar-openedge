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
package eu.rssw.listing;

import java.io.IOException;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestListing {

  @Test
  public static void testListing0() throws IOException {
    try {
      new ListingParser(Paths.get("src/test/resources/listing0.txt"), "listing0.txt");
    } catch (IOException caught) {
      return;
    }
    Assert.fail("No IOException caught");
  }

  @Test
  public static void testListing1() throws IOException {
    ListingParser parser = new ListingParser(Paths.get("src/test/resources/listing1.txt"), "listing1.txt");
    Assert.assertEquals(parser.getTransactionBlocks().size(), 4);
    Assert.assertEquals(parser.getMainBlock().getBuffers().size(), 3);
    Assert.assertEquals(parser.getMainBlock().getFrames().size(), 1);
  }

  @Test
  public static void testListing2() throws IOException {
    ListingParser parser = new ListingParser(Paths.get("src/test/resources/listing2.txt"), "listing2.txt");
    Assert.assertEquals(parser.getTransactionBlocks().size(), 0);
    Assert.assertEquals(parser.getMainBlock().getBuffers().size(), 4);
    // Find last and penultimate entry
    CodeBlock block = null;
    CodeBlock penult = null;
    for (CodeBlock b : parser.getBlocks()) {
      penult = block;
      block = b;
    }
    Assert.assertNotNull(penult);
    Assert.assertEquals(penult.getLineNumber(), -1);
    Assert.assertEquals(penult.getLabel(), "");

    Assert.assertNotNull(block);
    Assert.assertEquals(block.getLineNumber(), -32666);
    Assert.assertEquals(block.getLabel(), "Procedure foo-bar");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testListing3() throws IOException {
    new ListingParser(Paths.get("src/test/resources/listing 3.txt"), "listing 3.txt");
  }

  @Test
  public static void testListing4() throws IOException {
    new ListingParser(Paths.get("src/test/resources/listing4.txt"), "listing4.txt");
  }
}
