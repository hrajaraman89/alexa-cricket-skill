package cricketskill.api;

import java.util.Optional;
import org.junit.Test;


public class CricketApiClientTest {

  @Test
  public void testGetDetails()
      throws Exception {

    new CricketApiClient(i -> Optional.empty())
        .getDetails()
        .forEach(System.out::println);
  }
}