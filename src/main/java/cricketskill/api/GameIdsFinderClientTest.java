package cricketskill.api;

import org.junit.Test;


public class GameIdsFinderClientTest {

  @Test
  public void testGetGameIds()
      throws Exception {

    System.out.println(new GameIdsFinderClient().getGameIds());
  }
}