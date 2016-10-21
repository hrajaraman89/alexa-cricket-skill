package cricketskill.api;

import com.amazonaws.Protocol;
import cricketskill.db.DynamoDbClient;
import cricketskill.model.GameDetailClientResult;
import org.junit.Test;


public class CricketApiClientTest {

  @Test
  public void testGetDetails()
      throws Exception {

    GameDetailClientResult details = new CricketApiClient(i -> new DynamoDbClient(Protocol.HTTPS).getGames(i))
        .getDetails(0, 3);

    System.out.println(details);
  }
}