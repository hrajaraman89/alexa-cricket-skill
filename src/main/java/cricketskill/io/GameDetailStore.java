package cricketskill.io;

import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import cricketskill.model.GameDetail;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class GameDetailStore extends DynamoStore {
  private static final String GAME_DETAIL = "CricketGameDetail";

  // use HTTPS if you are testing locally
  public GameDetailStore(Protocol protocol) {
    super(protocol);
  }

  public Map<Integer, GameDetail> getGames(Set<Integer> id) {

    String tableName = GAME_DETAIL;

    String primaryKeyName = "id";

    BatchGetItemOutcome batchGetItemOutcome = batchGet(id, tableName, primaryKeyName);

    return batchGetItemOutcome
        .getTableItems()
        .getOrDefault(tableName, Lists.newArrayList())
        .stream()
        .map(GameDetailStore::toGame)
        .collect(Collectors.toMap(GameDetail::getId, gd -> gd));
  }



  private static GameDetail toGame(Item i) {
    return new Gson().fromJson(i.toJSON(), GameDetail.class);
  }
}
