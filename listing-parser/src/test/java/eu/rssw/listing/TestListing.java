package eu.rssw.listing;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import eu.rssw.listing.ListingParser;

public class TestListing {

  @Test
  public static void testListing0() throws IOException {
    try {
      new ListingParser(new File("src/test/resources/listing0.txt"));
    } catch (IOException caught) {
      return;
    }
    Assert.fail("No IOException caught");
  }

  @Test
  public static void testListing1() throws IOException {
    ListingParser parser = new ListingParser(new File("src/test/resources/listing1.txt"));
    Assert.assertEquals(parser.getTransactionBlocks().size(), 4);
    Assert.assertEquals(parser.getMainBlock().getBuffers().size(), 3);
    Assert.assertEquals(parser.getMainBlock().getFrames().size(), 1);
  }

  @Test
  public static void testListing2() throws IOException {
    ListingParser parser = new ListingParser(new File("src/test/resources/listing2.txt"));
    Assert.assertEquals(parser.getTransactionBlocks().size(), 0);
    Assert.assertEquals(parser.getMainBlock().getBuffers().size(), 4);
  }

}
