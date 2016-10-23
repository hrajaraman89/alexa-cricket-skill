package cricketskill;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.User;
import com.amazonaws.Protocol;
import cricketskill.io.FavoriteTeamStore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class CricketSpeechletTest {

  @Test
  public void test2() {

    String userId = String.valueOf(System.currentTimeMillis());

    Session session = Session.builder()
        .withSessionId("session")
        .withUser(User.builder()
            .withUserId(userId)
            .build())
        .build();

    session.setAttribute(CricketSpeechlet.START_KEY, 0);

    new FavoriteTeamStore(Protocol.HTTPS)
        .addFavoriteTeam(userId, "Namibia");

    CricketSpeechlet cricketSpeechlet = new CricketSpeechlet(Protocol.HTTPS);
    SpeechletResponse result = cricketSpeechlet.handleCurrentScoreIntent(session);

    assertTrue(result != null);
  }
}