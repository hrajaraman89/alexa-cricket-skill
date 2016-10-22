package cricketskill.api;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

  private final Function<Set<Integer>, Map<Integer, GameDetail>> _cacheFunction;

  public GameDetailClient(Function<Set<Integer>, Map<Integer, GameDetail>> cacheFunction) {
    _cacheFunction = cacheFunction;
  }

  public GameDetailClientResult getDetails(int start, int count) {

    List<Integer> gameIds = new GameIdsFinderClient().getGameIds();

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

    Map<Integer, GameDetail> result = Optional.ofNullable(_cacheFunction.apply(ids))
        .orElse(Maps.newHashMap());

    return new GameDetailClientResult(total, result);
  }
}
