package cricketskill.io;

import com.amazonaws.Protocol;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class FavoriteTeamStoreTest {

  @Test
  public void testGetFavoriteTeams()
      throws Exception {

    Set<String> result = newStore().getFavoriteTeams("userId");
    assertEquals(Sets.newHashSet(), result);
  }

  @Test
  public void testAddFavoriteTeam()
      throws Exception {
    String userId = String.valueOf(System.currentTimeMillis());

    FavoriteTeamStore store = newStore();

    store.addFavoriteTeam(userId, "India");

    assertEquals(ImmutableSet.of("India"), store.getFavoriteTeams(userId));
  }

  private FavoriteTeamStore newStore() {
    return new FavoriteTeamStore(Protocol.HTTPS);
  }
}