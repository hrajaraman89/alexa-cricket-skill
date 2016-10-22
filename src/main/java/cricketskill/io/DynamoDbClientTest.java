package cricketskill.io;

import com.amazonaws.Protocol;
import com.google.common.collect.Sets;
import cricketskill.model.GameDetail;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DynamoDbClientTest {

  @Test
  public void testGetGames()
      throws Exception {

    Map<Integer, GameDetail> result = new DynamoDbClient(Protocol.HTTPS)
        .getGames(Sets.newHashSet(1029825, 1053511, 1234, 1060249));

    assertEquals(result.size(), 3);
  }
}