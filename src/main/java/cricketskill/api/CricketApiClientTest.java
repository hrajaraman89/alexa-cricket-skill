package cricketskill.api;

import org.junit.Test;


public class CricketApiClientTest {

  @Test
  public void testGetDetails()
      throws Exception {

    new CricketApiClient().getDetails().forEach(System.out::println);
  }
}