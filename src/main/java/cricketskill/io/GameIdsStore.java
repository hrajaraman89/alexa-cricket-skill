package cricketskill.io;

import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class GameIdsStore extends DynamoStore {
  private static final String GAME_IDS = "CricketGameIds";

  protected GameIdsStore(Protocol protocol) {
    super(protocol);
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
}
