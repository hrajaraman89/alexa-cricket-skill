package cricketskill;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.Protocol;
import org.junit.Test;


public class CricketSpeechletTest {

  @Test
  public void test2() {
    Session session = Session.builder()
        .withSessionId("session")
        .build();
    session.setAttribute(CricketSpeechlet.START_KEY, 0);

    SpeechletResponse result = new CricketSpeechlet(Protocol.HTTPS).getCurrentScoreResponse(session);

    System.out.println(result);
  }
}