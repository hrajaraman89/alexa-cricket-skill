package cricketskill.api;

import com.amazonaws.Protocol;
import cricketskill.io.Stores;
import cricketskill.model.GameDetailClientResult;
import org.junit.Test;


public class GameDetailClientTest {

  @Test
  public void testGetDetails()
      throws Exception {
    GameDetailClientResult details = new GameDetailClient(new Stores(Protocol.HTTPS))
        .getDetails(0, 3);

    System.out.println(details);
  }
}