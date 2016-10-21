package cricketskill;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import cricketskill.api.CricketApiClient;
import cricketskill.db.DynamoDbClient;
import cricketskill.model.GameDetail;
import cricketskill.model.MatchStatus;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CricketSpeechlet implements Speechlet {
  private static final Logger log = LoggerFactory.getLogger(CricketSpeechlet.class);
  private final CricketApiClient _client;
  private final DynamoDbClient _dbClient;

  public CricketSpeechlet() {
    _dbClient = new DynamoDbClient();
    _client = new CricketApiClient(_dbClient::getGame);
  }

  @Override
  public void onSessionStarted(final SessionStartedRequest request, final Session session)
      throws SpeechletException {
    log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());
    // any initialization logic goes here
  }

  @Override
  public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
      throws SpeechletException {
    log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());
    return getWelcomeResponse();
  }

  @Override
  public SpeechletResponse onIntent(final IntentRequest request, final Session session)
      throws SpeechletException {
    log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());

    Intent intent = request.getIntent();
    String intentName = (intent != null) ? intent.getName() : null;

    if ("CurrentScoreIntent".equals(intentName)) {
      return getCurrentScoreResponse();
    } else if ("AMAZON.HelpIntent".equals(intentName)) {
      return getHelpResponse();
    } else if ("OscarIntent".equals(intentName)) {
      return newResponse("Oscar... is the best team, as long as they don't do work tomorrow.", "Oscar");
    } else {
      throw new SpeechletException("Invalid Intent");
    }
  }

  @Override
  public void onSessionEnded(final SessionEndedRequest request, final Session session)
      throws SpeechletException {
    log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());
    // any cleanup logic goes here
  }

  private SpeechletResponse getWelcomeResponse() {
    String speechText = "Welcome to the Alexa Skills Kit, you can say what is the current score";

    // Create the Simple card content.
    return newResponse(speechText, "Current Score");
  }

  public SpeechletResponse getCurrentScoreResponse() {

    log.info("Getting current response");

    long start = System.currentTimeMillis();

    List<GameDetail> result = _client.getDetails();

    result.stream()
        .filter(g -> !g.isCachedResult())
        .map(s -> {
          log.info("{} is not cached. Writing to DB", s.getId());
          return s;
        })
        .forEach(_dbClient::updateGame);

    StringBuilder sb = new StringBuilder(String.format("There are a total of %d games. ", result.size()));

    for (int i = 0; i < result.size(); i++) {
      sb.append(" ")
          .append(i + 1)
          .append(". ");

      appendDetailToStringBuilder(sb, result.get(i));
    }

    long end = System.currentTimeMillis();

    sb.append("\nApi response took ")
        .append((end - start))
        .append(" milliseconds.");

    String speechText = sb.toString();

    log.info("Speech text: {}", speechText);

    // Create the Simple card content.
    SimpleCard card = new SimpleCard();
    card.setTitle("Current Score");
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);

    return SpeechletResponse.newTellResponse(speech, card);
  }

  private static void appendDetailToStringBuilder(StringBuilder sb, GameDetail gd) {
    sb.append(gd.getTeamA().getName())
        .append(String.format(" %s ", gd.getStatus() == MatchStatus.COMPLETE ? "played" : "is playing"))
        .append(gd.getTeamB().getName())
        .append(" at ")
        .append(gd.getVenue())
        .append(". ")
        .append(gd.getLiveStatus())
        .append(". ");
  }

  /**
   * Creates a {@code SpeechletResponse} for the help intent.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getHelpResponse() {
    String speechText = "You can say give me an update on the games to me!";
    String title = "Current Score - Help";
    return newResponse(speechText, title);
  }

  private SpeechletResponse newResponse(String speechText, String title) {
    // Create the Simple card content.
    SimpleCard card = new SimpleCard();
    card.setTitle(title);
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);

    // Create reprompt
    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(speech);

    return SpeechletResponse.newAskResponse(speech, reprompt, card);
  }
}
