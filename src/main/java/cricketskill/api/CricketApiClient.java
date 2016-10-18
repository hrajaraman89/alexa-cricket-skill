package cricketskill.api;

import com.google.common.collect.Lists;
import cricketskill.model.GameDetail;
import cricketskill.model.MatchStatus;
import cricketskill.model.Team;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import static java.util.Optional.empty;


public class CricketApiClient {
  private static final String API_URL_TO_GET_IDS = "http://cricapi.com/api/cricket/";
  private static final String API_URL_TO_GET_MATCH_DETAIL_FORMAT =
      "http://www.espncricinfo.com/ci/engine/match/%d.json";

  public List<GameDetail> getDetails() {

    List<Integer> gameIds = getGameIds();

    if (gameIds.isEmpty()) {
      return Lists.newArrayList();
    }

    return Stream.of(gameIds.get(0), gameIds.get(1))
        .map(this::getGameDetail)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private Optional<GameDetail> getGameDetail(int id) {
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

    gameIds.add(0, 1030217);
    gameIds.add(1, 1004251);

    return gameIds;
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
