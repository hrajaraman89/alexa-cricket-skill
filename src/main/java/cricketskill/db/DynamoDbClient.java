package cricketskill.db;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import cricketskill.model.GameDetail;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DynamoDbClient {
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbClient.class);

  public static final String GAME_DETAIL = "CricketGameDetail";
  private final DynamoDB _dynamoDB;

  // use HTTPS if you are testing locally
  public DynamoDbClient(Protocol protocol) {
    AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ClientConfiguration().withProtocol(protocol));

    this._dynamoDB = new DynamoDB(client);
  }

  public void updateGames(List<GameDetail> gameDetails) {

    if (gameDetails.isEmpty()) {
      return;
    }

    TableWriteItems batchWrite = new TableWriteItems(GAME_DETAIL);

    gameDetails.stream()
        .map(this::toItem)
        .forEach(batchWrite::addItemToPut);

    BatchWriteItemOutcome result = _dynamoDB.batchWriteItem(batchWrite);

    LOG.info("Unprocessed items {} ", result.getUnprocessedItems());
  }

  private Item toItem(GameDetail gd) {

    int id = gd.getId();
    return new Item()
        .withPrimaryKey(toPrimaryKey(id))
        .with("liveStatus", gd.getLiveStatus())
        .with("status", gd.getStatus().toString())
        .with("winnerId", gd.getWinnerId())
        .with("lastUpdated", System.currentTimeMillis())
        .with("venue", gd.getVenue())
        .withJSON("teamA", gd.getTeamA().toJson())
        .withJSON("teamB", gd.getTeamB().toJson());
  }

  private GameDetail toGame(Item i) {
    return new Gson().fromJson(i.toJSON(), GameDetail.class);
  }

  private static PrimaryKey toPrimaryKey(int id) {
    return new PrimaryKey("id", id);
  }

  public Map<Integer, GameDetail> getGames(Set<Integer> id) {

    TableKeysAndAttributes batchGetAttributes = new TableKeysAndAttributes(GAME_DETAIL);

    id.stream()
        .map(i -> new KeyAttribute("id", i))
        .map(PrimaryKey::new)
        .forEach(batchGetAttributes::addPrimaryKey);

    return _dynamoDB.batchGetItem(batchGetAttributes)
        .getTableItems()
        .getOrDefault(GAME_DETAIL, Lists.newArrayList())
        .stream()
        .map(this::toGame)
        .collect(Collectors.toMap(GameDetail::getId, gd -> gd));
  }
}
