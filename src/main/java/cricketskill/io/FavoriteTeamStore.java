package cricketskill.io;

import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.google.common.collect.Lists;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class FavoriteTeamStore extends DynamoStore {
  protected FavoriteTeamStore(Protocol protocol) {
    super(protocol, "FavoriteTeams");
  }

  public Set<String> getFavoriteTeams(String userId) {
    Item item = get(new GetItemSpec().withPrimaryKey(new PrimaryKey("userId", userId)));

    return Optional.ofNullable(item)
        .map(i -> i.getList("favoriteTeams"))
        .orElse(Lists.newArrayList())
        .stream()
        .map(Object::toString)
        .collect(Collectors.toSet());
  }

  public boolean addFavoriteTeam(String userId, String team) {
    Set<String> currentTeams = getFavoriteTeams(userId);

    if (currentTeams.contains(team)) {
      return true;
    }

    currentTeams.add(team);

    Item i = toItem(userId, currentTeams);

    put(i);

    return true;
  }

  private Item toItem(String userId, Set<String> teams) {
    return new Item()
        .with("userId", userId)
        .withList("favoriteTeams", Lists.newArrayList(teams));
  }
}
