package cricketskill.common;

public class CallTimeTracker {
  private final String operation;
  private long start;
  private long end;

  public CallTimeTracker(String operation) {
    this.operation = operation;
  }

  public CallTimeTracker tic() {
    start = System.currentTimeMillis();
    return this;
  }

  public CallTimeTracker toc() {

    end = System.currentTimeMillis();
    return this;
  }

  public String toString() {
    return String.format("%s took %d ms", operation, (end - start));
  }
}
