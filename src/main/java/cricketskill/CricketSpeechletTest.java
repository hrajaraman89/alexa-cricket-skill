package cricketskill;

import com.amazon.speech.speechlet.SpeechletResponse;
import org.junit.Test;


public class CricketSpeechletTest {

  @Test
  public void test2() {
    SpeechletResponse result = new CricketSpeechlet().getCurrentScoreResponse();

    System.out.println(result);
  }
}