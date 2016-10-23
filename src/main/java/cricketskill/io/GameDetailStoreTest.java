package cricketskill.io;

import com.amazonaws.Protocol;
import com.google.common.collect.ImmutableSet;
import cricketskill.model.GameDetail;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;


public class GameDetailStoreTest {

  @Test
  public void testGetGamesByTeam()
      throws Exception {

    List<GameDetail> result = new GameDetailStore(Protocol.HTTPS)
        .getGamesByTeam(ImmutableSet.of("India", "New Zealand"));

    System.out.println(result);

  }
}