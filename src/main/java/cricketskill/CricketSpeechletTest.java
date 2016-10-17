package cricketskill;

import com.amazon.speech.speechlet.SpeechletResponse;
import java.io.IOException;
import org.junit.Test;
import org.w3c.dom.NodeList;
import us.monoid.web.Resty;

import static org.junit.Assert.*;


public class CricketSpeechletTest {

  @Test
  public void test()
      throws Exception {
    NodeList result = new Resty().xml("http://synd.cricbuzz.com/j2me/1.0/livematches.xml").get("mchdata/match");

    System.out.println(result);
  }

  @Test
  public void test2() {
    SpeechletResponse result = new CricketSpeechlet().getCurrentScoreResponse();

    System.out.println(result);
  }

}