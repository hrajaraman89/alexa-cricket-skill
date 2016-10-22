package cricketskill.io;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import cricketskill.model.GameDetail;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DynamoDbClient {
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbClient.class);

  private static final String GAME_DETAIL = "CricketGameDetail";
  private static final String GAME_IDS = "CricketGameIds";

  private final DynamoDB _dynamoDB;

  // use HTTPS if you are testing locally
  public DynamoDbClient(Protocol protocol) {
    AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ClientConfiguration().withProtocol(protocol));

    this._dynamoDB = new DynamoDB(client);
  }

  public List<Integer> getGameIds() {
    String tableName = GAME_IDS;

    BatchGetItemOutcome batchGetItemOutcome = batchGet(Sets.newHashSet("intl"), tableName, "id");

    return batchGetItemOutcome
        .getTableItems()
        .getOrDefault(tableName, Lists.newArrayList())
        .stream()
        .flatMap(this::toGameIds)
        .collect(Collectors.toList());
  }

  private Stream<Integer> toGameIds(Item item) {
    String gameIds = item.getJSON("gameIds");

    int[] ints = new Gson().fromJson(gameIds, int[].class);

    return Arrays.stream(ints).boxed();
  }

  public Map<Integer, GameDetail> getGames(Set<Integer> id) {

    String tableName = GAME_DETAIL;

    String primaryKeyName = "id";

    BatchGetItemOutcome batchGetItemOutcome = batchGet(id, tableName, primaryKeyName);

    return batchGetItemOutcome
        .getTableItems()
        .getOrDefault(tableName, Lists.newArrayList())
        .stream()
        .map(DynamoDbClient::toGame)
        .collect(Collectors.toMap(GameDetail::getId, gd -> gd));
  }

  private <T> BatchGetItemOutcome batchGet(Set<T> primaryKeys, String tableName, String primaryKeyName) {
    TableKeysAndAttributes batchGetAttributes = new TableKeysAndAttributes(tableName);

    primaryKeys.stream()
        .map(i -> new KeyAttribute(primaryKeyName, i))
        .map(PrimaryKey::new)
        .forEach(batchGetAttributes::addPrimaryKey);

    return _dynamoDB.batchGetItem(batchGetAttributes);
  }

  private static GameDetail toGame(Item i) {
    return new Gson().fromJson(i.toJSON(), GameDetail.class);
  }
}
