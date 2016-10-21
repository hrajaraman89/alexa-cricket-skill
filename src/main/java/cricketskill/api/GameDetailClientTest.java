package cricketskill.api;

import com.amazonaws.Protocol;
import cricketskill.io.DynamoDbClient;
import cricketskill.model.GameDetailClientResult;
import org.junit.Test;


public class GameDetailClientTest {

  @Test
  public void testGetDetails()
      throws Exception {

    GameDetailClientResult details = new GameDetailClient(i -> new DynamoDbClient(Protocol.HTTPS).getGames(i))
        .getDetails(0, 3);

    System.out.println(details);
  }
}