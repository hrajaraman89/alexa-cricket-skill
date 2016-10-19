package cricketskill.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.google.gson.Gson;
import cricketskill.model.GameDetail;
import java.util.Optional;


public class DynamoDbClient {

  private final Table _table;

  public DynamoDbClient() {
    AmazonDynamoDBClient client = new AmazonDynamoDBClient();

    DynamoDB dynamoDB = new DynamoDB(client);

    _table = dynamoDB.getTable("CricketGameDetail");

    System.out.println("Db client initiated");
  }

  public boolean updateGame(GameDetail gd) {
    Item item = toItem(gd);

    _table.putItem(item);
    return true;
  }

  private Item toItem(GameDetail gd) {

    int id = gd.getId();
    return new Item()
        .withPrimaryKey(toPrimaryKey(id))
        .with("liveStatus", gd.getLiveStatus())
        .with("status", gd.getStatus().toString())
        .with("winnerId", gd.getWinnerId())
        .with("venue", gd.getVenue())
        .withJSON("teamA", gd.getTeamA().toJson())
        .withJSON("teamB", gd.getTeamB().toJson());
  }

  private static PrimaryKey toPrimaryKey(int id) {
    return new PrimaryKey("id", id);
  }

  public Optional<GameDetail> getGame(int id) {
    Item i = _table.getItem(toPrimaryKey(id));

    return Optional.ofNullable(i)
        .map(Item::toJSON)
        .map(j -> new Gson().fromJson(j, GameDetail.class));
  }
}
