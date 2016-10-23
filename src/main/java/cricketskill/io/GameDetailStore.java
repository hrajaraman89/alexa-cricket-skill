package cricketskill.io;

import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import cricketskill.common.TrackerUtils;
import cricketskill.model.GameDetail;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GameDetailStore extends DynamoStore {

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

  public List<GameDetail> getGamesByTeam(Set<String> teams) {

    return TrackerUtils.withTracking(() -> getGamesByTeamInternal(teams), "Get games for favorite team", LOG);
  }

  private List<GameDetail> getGamesByTeamInternal(Set<String> teams) {
    if (teams.isEmpty()) {
      return Lists.newArrayList();
    }

    Map<String, AttributeValue> eav = Maps.newHashMap();

    int i = 1;
    for (String team : teams) {
      AttributeValue value = new AttributeValue().withS(team);
      eav.put(String.format(":val%d", i), value);
      eav.put(String.format(":val%d", i + 1), value);

      i += 2;
    }

    String conditionExpression = getConditionExpression(teams.size());

    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
        .withFilterExpression(conditionExpression)
        .withExpressionAttributeValues(eav);

    return scan(scanExpression, GameDetail.class);
  }

  private static String getConditionExpression(int numTeams) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < numTeams; i++) {
      sb.append(String.format("teamAName = :val%d or teamBName = :val%d %s ", 2 * i + 1, 2 * i + 2,
          i == numTeams - 1 ? "" : "or"));
    }

    return sb.toString();
  }

  private static GameDetail toGame(Item i) {
    return new Gson().fromJson(i.toJSON(), GameDetail.class);
  }
}
