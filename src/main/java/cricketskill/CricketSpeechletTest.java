package cricketskill;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.User;
import com.amazonaws.Protocol;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cricketskill.io.FavoriteTeamStore;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class CricketSpeechletTest {

  @Test
  public void test2() {

    String userId = String.valueOf(System.currentTimeMillis());

    Map<String, Object> attributes = Maps.newHashMap(ImmutableMap.of("seenGameIds", Sets.newHashSet(78807)));

    Session session = Session.builder()
        .withSessionId("session")
        .withAttributes(attributes)
        .withUser(User.builder()
            .withUserId(userId)
            .build())
        .build();

    new FavoriteTeamStore(Protocol.HTTPS)
        .addFavoriteTeam(userId, "West Indies");

    CricketSpeechlet cricketSpeechlet = new CricketSpeechlet(Protocol.HTTPS);
    SpeechletResponse result = cricketSpeechlet.handleCurrentScoreIntent(session, 2);

    assertTrue(result != null);
  }
}