package cricketskill.api;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import cricketskill.common.CallTimeTracker;
import cricketskill.model.GameDetail;
import cricketskill.model.MatchStatus;
import cricketskill.model.Team;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import static java.util.Optional.empty;


public class CricketApiClient {
  private static final Logger LOG = LoggerFactory.getLogger(CricketApiClient.class);

  private static final String API_URL_TO_GET_IDS = "http://cricapi.com/api/cricket/";
  private static final String API_URL_TO_GET_MATCH_DETAIL_FORMAT =
      "http://www.espncricinfo.com/ci/engine/match/%d.json";

  private final Function<Integer, Optional<GameDetail>> _cacheFunction;

  public CricketApiClient(Function<Integer, Optional<GameDetail>> cacheFunction) {
    _cacheFunction = cacheFunction;
  }

  public List<GameDetail> getDetails() {

    List<Integer> gameIds = getGameIds();

    if (gameIds.isEmpty()) {
      return Lists.newArrayList();
    }

    return gameIds.stream()
        .map(this::getGameDetail)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Optional<GameDetail> getGameDetail(int id) {
    return withTracking(() -> getGameDetailWithoutTracking(id), "Get Game Detail " + id);
  }

  private Optional<GameDetail> getGameDetailWithoutTracking(int id) {

    Optional<GameDetail> cacheResult = _cacheFunction.apply(id);

    if (cacheResult.isPresent()) {
      cacheResult.get().setCachedResult(true);
      return cacheResult;
    }

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

    String teamA = safeJsonOp(() -> match.getString("team1_name")).get();
    int teamAId = safeJsonOp(() -> match.getInt("team1_country_id")).get();

    String teamB = safeJsonOp(() -> match.getString("team2_name")).get();
    int teamBId = safeJsonOp(() -> match.getInt("team2_country_id")).get();

    int winner = safeJsonOp(() -> match.getInt("winner_team_id")).get();

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
        .setVenue(venue);

    return Optional.of(build);
  }

  private List<Integer> getGameIds() {
    return withTracking(this::getGameIdsWithoutTracking, "Get Game Ids");
  }

  private List<Integer> getGameIdsWithoutTracking() {

    UnsafeJsonOp<JSONArray> getArray = () -> (JSONArray) new Resty().json(API_URL_TO_GET_IDS).get("data");

    Optional<JSONArray> dataOptional = safeJsonOp(getArray);

    if (isEmpty(dataOptional)) {
      return Lists.newArrayList();
    }

    JSONArray data = dataOptional.get();

    List<Integer> gameIds = IntStream.range(0, data.length()).boxed()
        .map(d -> safeJsonOp(() -> data.getJSONObject(d)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(dataItem -> safeJsonOp(() -> dataItem.getInt("unique_id")))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    List<Integer> result = gameIds.subList(0, 5);

    LOG.info("Returning ids: {}", result);

    return result;
  }

  private static <T> T withTracking(Supplier<T> supplier, String operationName) {
    CallTimeTracker tracker = new CallTimeTracker(operationName).tic();

    T result = supplier.get();

    tracker.toc();

    LOG.info(tracker.toString());

    return result;
  }

  private static <T> Optional<T> safeJsonOp(UnsafeJsonOp<T> op) {
    try {
      return Optional.of(op.perform());
    } catch (Exception e) {
      e.printStackTrace();
      //TODO: LOG
    }

    return empty();
  }

  @FunctionalInterface
  private interface UnsafeJsonOp<T> {
    T perform()
        throws Exception;
  }

  private static <T> boolean isEmpty(Optional<T> optional) {
    return !optional.isPresent();
  }
}
