package cricketskill.api;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cricketskill.model.GameDetail;
import cricketskill.model.GameDetailClientResult;
import cricketskill.model.MatchStatus;
import cricketskill.model.Team;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import static cricketskill.common.OptionalUtils.isEmpty;
import static cricketskill.common.TrackerUtils.withTracking;
import static cricketskill.common.UnsafeJsonOp.safeJsonOp;
import static java.util.Optional.empty;


public class GameDetailClient {
  private static final Logger LOG = LoggerFactory.getLogger(GameDetailClient.class);

  private static final String API_URL_TO_GET_MATCH_DETAIL_FORMAT =
      "http://www.espncricinfo.com/ci/engine/match/%d.json";

  private final Function<Set<Integer>, Map<Integer, GameDetail>> _cacheFunction;

  public GameDetailClient(Function<Set<Integer>, Map<Integer, GameDetail>> cacheFunction) {
    _cacheFunction = cacheFunction;
  }

  public GameDetailClientResult getDetails(int start, int count) {

    List<Integer> gameIds = new GameIdsFinderClient().getGameIds();

    if (gameIds.isEmpty()) {
      return new GameDetailClientResult(0, Maps.newHashMap(), Sets.newHashSet());
    }

    if (start >= gameIds.size()) {
      return new GameDetailClientResult(0, Maps.newHashMap(), Sets.newHashSet());
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

    Map<Integer, GameDetail> cacheResult = Optional.ofNullable(_cacheFunction.apply(ids))
        .orElse(Maps.newHashMap());

    Set<Integer> processedIds = cacheResult.keySet();

    Sets.SetView<Integer> keysNotInCache = Sets.difference(ids, processedIds);

    LOG.info("Following keys not in cache: {}", keysNotInCache);

    Map<Integer, GameDetail> uncachedResult = keysNotInCache.stream()
        .map(this::fetchFromApi)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toMap(GameDetail::getId, gd -> gd));

    Map<Integer, GameDetail> result = Maps.newHashMap();

    result.putAll(cacheResult);
    result.putAll(uncachedResult);

    return new GameDetailClientResult(total, result, keysNotInCache);
  }

  private Optional<GameDetail> fetchFromApi(int id) {
    LOG.info("{} not cached, fetching from API", id);

    String url = String.format(API_URL_TO_GET_MATCH_DETAIL_FORMAT, id);

    Optional<JSONResource> jsonOptional = safeJsonOp(() -> new Resty().json(url));

    if (isEmpty(jsonOptional)) {
      //TODO: log
      return empty();
    }

    JSONResource json = jsonOptional.get();

    Optional<JSONObject> matchOptional = safeJsonOp(() -> (JSONObject) json.get("match"));

    if (isEmpty(matchOptional)) {
      //TODO: log
      return empty();
    }

    JSONObject match = matchOptional.get();

    String venue = safeJsonOp(() -> match.getString("ground_name")).get();
    String shortVenue = safeJsonOp(() -> match.getString("ground_small_name")).get();

    String teamA = safeJsonOp(() -> match.getString("team1_name")).get();
    int teamAId = safeJsonOp(() -> match.getInt("team1_country_id")).get();

    String teamB = safeJsonOp(() -> match.getString("team2_name")).get();
    int teamBId = safeJsonOp(() -> match.getInt("team2_country_id")).get();

    int winner = safeJsonOp(() -> match.getInt("winner_team_id"))
        .orElse(0);

    String liveStatus = safeJsonOp(() -> ((JSONObject) json.get("live")).getString("status")).get();

    String matchStatus = safeJsonOp(() -> match.getString("match_status")).get().toUpperCase();
    MatchStatus status = MatchStatus.valueOf(matchStatus);

    GameDetail build = new GameDetail()
        .setId(id)
        .setTeamA(new Team(teamA, teamAId))
        .setTeamB(new Team(teamB, teamBId))
        .setWinnerId(winner)
        .setStatus(status)
        .setLiveStatus(liveStatus)
        .setShortVenue(shortVenue)
        .setVenue(venue);

    return Optional.of(build);
  }
}
