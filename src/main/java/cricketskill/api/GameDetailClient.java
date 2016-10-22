package cricketskill.api;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cricketskill.io.DynamoDbClient;
import cricketskill.model.GameDetail;
import cricketskill.model.GameDetailClientResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cricketskill.common.TrackerUtils.withTracking;


public class GameDetailClient {
  private static final Logger LOG = LoggerFactory.getLogger(GameDetailClient.class);

  private final DynamoDbClient _dynamoDbClient;

  public GameDetailClient(DynamoDbClient client) {
    this._dynamoDbClient = client;
  }

  public GameDetailClientResult getDetails(int start, int count) {

    List<Integer> gameIds = withTracking(_dynamoDbClient::getGameIds, "Get Game Ids", LOG);

    if (gameIds.isEmpty()) {
      return new GameDetailClientResult(0, Maps.newHashMap());
    }

    if (start >= gameIds.size()) {
      return new GameDetailClientResult(0, Maps.newHashMap());
    }

    int end = Math.min((start + count), gameIds.size());

    LOG.info("Fetching games # {} to {}", start, end);

    List<Integer> integers = gameIds.subList(start, end);
    return getGameDetail(Sets.newHashSet(integers), gameIds.size());
  }

  private GameDetailClientResult getGameDetail(Set<Integer> ids, int total) {
    return withTracking(() -> getGameDetailWithoutTracking(ids, total), "Get Game Detail " + ids, LOG);
  }

  private GameDetailClientResult getGameDetailWithoutTracking(Set<Integer> ids, int total) {

    Map<Integer, GameDetail> result = Optional.ofNullable(_dynamoDbClient.getGames(ids))
        .orElse(Maps.newHashMap());

    return new GameDetailClientResult(total, result);
  }
}
