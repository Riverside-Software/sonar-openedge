package eu.rssw.antlr.profiler;

import java.util.Comparator;

public class LineData {
  private final int lineNumber;
  private final int execCount;
  private final float actualTime;
  private final float cumulativeTime;

  public LineData(int lineNumber, int execCount, float actualTime, float cumulativeTime) {
    this.lineNumber = lineNumber;
    this.execCount = execCount;
    this.actualTime = actualTime;
    this.cumulativeTime = cumulativeTime;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getExecCount() {
    return execCount;
  }

  public float getActualTime() {
    return actualTime;
  }

  public float getCumulativeTime() {
    return cumulativeTime;
  }

  @Override
  public int hashCode() {
    return lineNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LineData) {
      return ((LineData) obj).lineNumber == lineNumber;
    }

    return false;
  }

  @Override
  public String toString() {
    return lineNumber + (execCount == 0 ? "" : " x" + execCount);
  }

  public static class CumulativeTimeComparator implements Comparator<LineData> {

    @Override
    public int compare(LineData o1, LineData o2) {
      return ((int) (o1.getCumulativeTime() * 1000000)) - ((int) (o2.getCumulativeTime() * 1000000));
    }

  }
}
