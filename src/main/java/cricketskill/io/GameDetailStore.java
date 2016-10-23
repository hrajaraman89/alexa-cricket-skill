package cricketskill.io;

import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.gson.Gson;
import cricketskill.model.GameDetail;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GameDetailStore extends DynamoStore {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(GameDetailStore.class);

  // use HTTPS if you are testing locally
  public GameDetailStore(Protocol protocol) {
    super(protocol, "CricketGameDetail");
  }

  public Map<Integer, GameDetail> getGames(Set<Integer> id) {

    String primaryKeyName = "id";

    return batchGet(id, primaryKeyName)
        .stream()
        .map(GameDetailStore::toGame)
        .collect(Collectors.toMap(GameDetail::getId, gd -> gd));
  }

  private static GameDetail toGame(Item i) {
    return new Gson().fromJson(i.toJSON(), GameDetail.class);
  }
}
