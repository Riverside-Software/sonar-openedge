package eu.rssw.pct;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

public class PLReaderTest {

  @Test
  public void testRCodeInPL() throws IOException, InvalidRCodeException {
    PLReader pl = new PLReader(new File("src/test/resources/ablunit.pl"));
    Assert.assertNotNull(pl.getEntry("OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r"));
    RCodeInfo rci = new RCodeInfo(pl.getInputStream(pl.getEntry("OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r")));
    Assert.assertTrue(rci.isClass());
    Assert.assertTrue(rci.getUnit().getMethods().size() > 0);
    Assert.assertTrue(rci.getUnit().getProperties().size() > 0);
    Assert.assertTrue(rci.getUnit().getTables().size() == 0);
  }

}
