package cricketskill.api;

import com.amazonaws.Protocol;
import cricketskill.io.DynamoDbClient;
import cricketskill.model.GameDetailClientResult;
import org.junit.Test;


public class GameDetailClientTest {

  @Test
  public void testGetDetails()
      throws Exception {
    DynamoDbClient dynamoDbClient = new DynamoDbClient(Protocol.HTTPS);
    GameDetailClientResult details = new GameDetailClient(dynamoDbClient)
        .getDetails(0, 3);

    System.out.println(details);
  }
}