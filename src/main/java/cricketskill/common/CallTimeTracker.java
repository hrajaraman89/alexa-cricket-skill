package cricketskill.common;

public class CallTimeTracker {
  private final String _operation;
  private long _start;
  private long _end;

  public CallTimeTracker(String operation) {
    this._operation = operation;
  }

  public CallTimeTracker tic() {
    _start = System.currentTimeMillis();
    return this;
  }

  public CallTimeTracker toc() {
    _end = System.currentTimeMillis();
    return this;
  }

  public long elapsed() {
    return (_end == 0 ? System.currentTimeMillis() : _end) - _start;
  }

  public String toString() {
    return String.format("%s took %d ms", _operation, (_end - _start));
  }
}
