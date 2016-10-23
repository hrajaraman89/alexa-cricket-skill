package cricketskill.io;

import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.gson.Gson;
import cricketskill.model.CricketGameDetail;
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

  public Map<Integer, CricketGameDetail> getGames(Set<Integer> id) {

    String primaryKeyName = "id";

    return batchGet(id, primaryKeyName)
        .stream()
        .map(GameDetailStore::toGame)
        .collect(Collectors.toMap(CricketGameDetail::getId, gd -> gd));
  }

  private static CricketGameDetail toGame(Item i) {
    return new Gson().fromJson(i.toJSON(), CricketGameDetail.class);
  }
}
