package cricketskill.api;

import com.amazonaws.Protocol;
import cricketskill.io.Stores;
import cricketskill.model.GameDetailClientResult;
import org.junit.Test;


public class CricketGameDetailClientTest {

  @Test
  public void testGetDetails()
      throws Exception {
    GameDetailClientResult details = new GameDetailClient(new Stores(Protocol.HTTPS))
        .getDetails();

    System.out.println(details);
  }
}