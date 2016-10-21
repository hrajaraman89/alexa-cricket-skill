package cricketskill.common;

import java.util.Optional;


public class OptionalUtils {
  private OptionalUtils() {
  }

  public static <T> boolean isEmpty(Optional<T> optional) {
    return !optional.isPresent();
  }
}
