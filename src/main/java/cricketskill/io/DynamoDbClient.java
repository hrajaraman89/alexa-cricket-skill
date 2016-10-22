package cricketskill.io;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import cricketskill.model.GameDetail;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DynamoDbClient {
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbClient.class);

  private static final String GAME_DETAIL = "CricketGameDetail";
  private final DynamoDB _dynamoDB;

  // use HTTPS if you are testing locally
  public DynamoDbClient(Protocol protocol) {
    AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ClientConfiguration().withProtocol(protocol));

    this._dynamoDB = new DynamoDB(client);
  }

  private GameDetail toGame(Item i) {
    return new Gson().fromJson(i.toJSON(), GameDetail.class);
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
