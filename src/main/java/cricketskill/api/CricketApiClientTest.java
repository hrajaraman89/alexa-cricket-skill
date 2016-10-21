package cricketskill.api;

import com.amazonaws.Protocol;
import cricketskill.db.DynamoDbClient;
import java.util.Optional;
import org.junit.Test;


public class CricketApiClientTest {

  @Test
  public void testGetDetails()
      throws Exception {

    new CricketApiClient(i -> new DynamoDbClient(Protocol.HTTPS).getGame(i))
        .getDetails()
        .forEach(System.out::println);
  }
}