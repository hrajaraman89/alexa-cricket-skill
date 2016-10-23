package cricketskill.io;

import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class GameIdsStore extends DynamoStore {

  protected GameIdsStore(Protocol protocol) {
    super(protocol, "CricketGameIds");
  }

  public List<Integer> getGameIds() {

    return batchGet(Sets.newHashSet("intl"), "id")
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
