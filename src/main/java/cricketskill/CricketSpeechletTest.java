package cricketskill;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.Protocol;
import org.junit.Test;


public class CricketSpeechletTest {

  @Test
  public void test2() {
    SpeechletResponse result = new CricketSpeechlet(Protocol.HTTPS).getCurrentScoreResponse();

    System.out.println(result);
  }
}