package cricketskill.api;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cricketskill.io.Stores;
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

  private final Stores _stores;

  public GameDetailClient(Stores stores) {
    _stores = stores;
  }

  public GameDetailClientResult getDetails() {

    List<Integer> gameIds = withTracking(_stores.getGameIdsStore()::getGameIds, "Get Game Ids", LOG);

    if (gameIds.isEmpty()) {
      return new GameDetailClientResult(0, Maps.newHashMap());
    }

    return getGameDetail(Sets.newHashSet(gameIds), gameIds.size());
  }

  private GameDetailClientResult getGameDetail(Set<Integer> ids, int total) {
    return withTracking(() -> getGameDetailWithoutTracking(ids, total), "Get Game Detail " + ids, LOG);
  }

  private GameDetailClientResult getGameDetailWithoutTracking(Set<Integer> ids, int total) {

    Map<Integer, GameDetail> result = Optional.ofNullable(_stores.getGameDetailStore().getGames(ids))
        .orElse(Maps.newHashMap());

    return new GameDetailClientResult(total, result);
  }
}
