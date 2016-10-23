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

    Map<String, Object> attributes = Maps.newHashMap(ImmutableMap.of("seenGameIds", Sets.newHashSet(76937)));

    Session session = Session.builder()
        .withSessionId("session")
        .withAttributes(attributes)
        .withUser(User.builder()
            .withUserId(userId)
            .build())
        .build();

    session.setAttribute(CricketSpeechlet.START_KEY, 4);

    new FavoriteTeamStore(Protocol.HTTPS)
        .addFavoriteTeam(userId, "Namibia");

    CricketSpeechlet cricketSpeechlet = new CricketSpeechlet(Protocol.HTTPS);
    SpeechletResponse result = cricketSpeechlet.handleCurrentScoreIntent(session);

    assertTrue(result != null);
  }
}