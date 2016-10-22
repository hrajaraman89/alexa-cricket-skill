package cricketskill.common;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.empty;


@FunctionalInterface
public interface UnsafeJsonOp<T> {
  Logger LOG = LoggerFactory.getLogger(UnsafeJsonOp.class);

  T perform()
      throws Exception;

  static <T> Optional<T> safeJsonOp(UnsafeJsonOp<T> op) {
    try {
      return Optional.of(op.perform());
    } catch (Exception e) {
      LOG.warn("Error parsing JSON {}", e.getMessage());
    }

    return empty();
  }
}