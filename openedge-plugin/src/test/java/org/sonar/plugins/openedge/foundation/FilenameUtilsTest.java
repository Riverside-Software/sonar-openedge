package org.sonar.plugins.openedge.foundation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;
import static org.sonar.plugins.openedge.foundation.FilenameUtils.getExtension;
import static org.sonar.plugins.openedge.foundation.FilenameUtils.removeExtension;
import static org.sonar.plugins.openedge.foundation.FilenameUtils.getBaseName;
import static org.sonar.plugins.openedge.foundation.FilenameUtils.getName;

import org.testng.annotations.Test;

public class FilenameUtilsTest {

  @Test
  public void testGetExtension() {
    assertEquals(getExtension("C:\\foo\\bar\\filename.txt"), "txt");
    assertEquals(getExtension("filename.txt"), "txt");
    assertEquals(getExtension("filename.t"), "t");
    assertEquals(getExtension("filename"), "");
    assertEquals(getExtension("foo.bar\\filename"), "");
    expectThrows(NullPointerException.class, () -> getExtension(null));
  }

  @Test
  public void testRemoveExtension() {
    assertEquals(removeExtension("C:\\foo\\bar\\filename.txt"), "C:\\foo\\bar\\filename");
    assertEquals(removeExtension("filename.txt"), "filename");
    assertEquals(removeExtension("filename.t"), "filename");
    assertEquals(removeExtension("filename"), "filename");
    expectThrows(NullPointerException.class, () -> removeExtension(null));
  }

  @Test
  public void testGetBaseName() {
    assertEquals(getBaseName("C:\\foo\\bar\\filename.txt"), "filename");
    assertEquals(getBaseName("filename.txt"), "filename");
    assertEquals(getBaseName("filename.t"), "filename");
    assertEquals(getBaseName("filename"), "filename");
    expectThrows(NullPointerException.class, () -> getBaseName(null));
  }

  @Test
  public void testGetName() {
    assertEquals(getName("C:\\foo\\bar\\filename.txt"), "filename.txt");
    assertEquals(getName("filename.txt"), "filename.txt");
    assertEquals(getName("filename.t"), "filename.t");
    assertEquals(getName("filename"), "filename");
    assertEquals(getName(null), null);
  }

}
