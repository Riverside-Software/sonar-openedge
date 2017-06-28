package eu.rssw.pct;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DataTypeTest {

  @Test
  public void test1() {
    Assert.assertEquals(DataType.getDataType(-1), DataType.UNKNOWN);
    Assert.assertEquals(DataType.getDataType(0), DataType.VOID);
    Assert.assertEquals(DataType.getDataType(48), DataType.RUNTYPE);
    Assert.assertEquals(DataType.getDataType(49), DataType.UNKNOWN);
  }

  @Test
  public void test2() {
    Assert.assertEquals(DataType.getDataType("-1"), DataType.UNKNOWN);
    Assert.assertEquals(DataType.getDataType("0"), DataType.VOID);
    Assert.assertEquals(DataType.getDataType("48"), DataType.RUNTYPE);
    Assert.assertEquals(DataType.getDataType("49"), DataType.UNKNOWN);
    // Really ?
    Assert.assertEquals(DataType.getDataType(""), DataType.CLASS);
    Assert.assertEquals(DataType.getDataType("Progress.Lang.Object"), DataType.CLASS);
  }

}
