package cricketskill.api;

import com.amazonaws.Protocol;
import com.google.common.collect.Sets;
import cricketskill.io.Stores;
import cricketskill.model.GameDetailClientResult;
import org.junit.Test;


public class GameDetailClientTest {

  @Test
  public void testGetDetails()
      throws Exception {
    GameDetailClientResult details = new GameDetailClient(new Stores(Protocol.HTTPS))
        .getDetails(0, 1, Sets.newHashSet());

    System.out.println(details);
  }
}