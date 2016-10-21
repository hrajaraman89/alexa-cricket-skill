package cricketskill.common;

import com.google.common.base.Supplier;
import org.slf4j.Logger;


public class TrackerUtils {
  private TrackerUtils() {
  }

  public static <T> T withTracking(Supplier<T> supplier, String operationName, Logger log) {
    CallTimeTracker tracker = new CallTimeTracker(operationName).tic();

    T result = supplier.get();

    tracker.toc();

    log.info(tracker.toString());

    return result;
  }
}
