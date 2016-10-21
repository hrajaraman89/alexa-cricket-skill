package cricketskill;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
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
import com.amazonaws.Protocol;
import com.google.common.collect.Lists;
import cricketskill.api.CricketApiClient;
import cricketskill.common.TrackerUtils;
import cricketskill.db.DynamoDbClient;
import cricketskill.model.GameDetail;
import cricketskill.model.GameDetailClientResult;
import cricketskill.model.MatchStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CricketSpeechlet implements Speechlet {
  private static final Logger LOG = LoggerFactory.getLogger(CricketSpeechlet.class);
  private static final long CACHE_TTL = 300000;
  private final CricketApiClient _client;
  private final DynamoDbClient _dbClient;

  private static final int PAGE_LENGTH = 3;
  static final String START_KEY = "start";

  public CricketSpeechlet() {
    this(Protocol.HTTP);
  }

  public CricketSpeechlet(Protocol protocol) {
    _dbClient = new DynamoDbClient(protocol);
    _client = new CricketApiClient(i -> _dbClient.getGames(i).entrySet().stream()
        .filter(es -> isCachedItemValid(es.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  private static boolean isCachedItemValid(GameDetail gd) {
    long now = System.currentTimeMillis();
    LOG.info("Looking at GameDetails id {} status {} lastUpdated {} now {} diff {}", gd.getId(), gd.getStatus(),
        gd.getLastUpdated(), now, (now - gd.getLastUpdated()));

    return gd.getStatus() == MatchStatus.COMPLETE || (now - gd.getLastUpdated()) <= CACHE_TTL;
  }

  @Override
  public void onSessionStarted(final SessionStartedRequest request, final Session session)
      throws SpeechletException {
    LOG.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());
    // any initialization logic goes here
  }

  @Override
  public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
      throws SpeechletException {
    LOG.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());
    return getWelcomeResponse();
  }

  @Override
  public SpeechletResponse onIntent(final IntentRequest request, final Session session)
      throws SpeechletException {
    LOG.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());

    Intent intent = request.getIntent();
    String intentName = (intent != null) ? intent.getName() : null;

    if ("CurrentScoreIntent".equals(intentName)) {
      return getCurrentScoreResponse(session);
    } else if ("AMAZON.HelpIntent".equals(intentName)) {
      return getHelpResponse();
    } else if ("OscarIntent".equals(intentName)) {
      return newAskResponse("Oscar... is the best team, as long as they don't do work tomorrow.", "Oscar");
    } else if ("NextScoreIntent".equals(intentName)) {
      Slot slot = intent.getSlot("NumberOfGames");

      LOG.info("Slot value from number of games is {}", slot.getValue());

      int count = Integer.valueOf(slot.getValue());
      return getCurrentScoreResponse(session, count);
    } else if ("EndIntent".equals(intentName)) {
      return newTellResponse("Okay, ending.", "Ending score tracker");
    } else {
      throw new SpeechletException("Invalid Intent");
    }
  }

  @Override
  public void onSessionEnded(final SessionEndedRequest request, final Session session)
      throws SpeechletException {
    LOG.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());
    // any cleanup logic goes here
  }

  public SpeechletResponse getCurrentScoreResponse(Session session) {
    return TrackerUtils.withTracking(() -> getCurrentScoreResponseInternal(session, PAGE_LENGTH), "Get current score",
        LOG);
  }

  public SpeechletResponse getCurrentScoreResponse(Session session, int count) {
    return TrackerUtils.withTracking(() -> getCurrentScoreResponseInternal(session, count), "Get current score", LOG);
  }

  private SpeechletResponse getCurrentScoreResponseInternal(Session session, int count) {

    LOG.info("Getting current response");

    int start = Optional.ofNullable(session.getAttribute(START_KEY))
        .map(Object::toString)
        .map(Integer::valueOf)
        .orElse(0);

    LOG.info("Session's start is {}", start);

    GameDetailClientResult details = _client.getDetails(start, count);

    List<GameDetail> result = Lists.newArrayList(details.getItems().values());

    session.setAttribute(START_KEY, (start + result.size()));

    LOG.info("Session's new start is {}", session.getAttribute(START_KEY));

    StringBuilder sb = new StringBuilder();

    sb.append("Giving updates on ");

    if (start == 0) {
      sb.append(String.format("first %d matches. ", result.size()));
    } else if (start + result.size() < details.getTotal()) {
      sb.append(String.format("matches %d to %d. ", start + 1, (start + result.size())));
    } else {
      sb.append(String.format("last %d matches. ", result.size()));
    }

    for (int i = 0; i < result.size(); i++) {
      sb.append(" ")
          .append(i + start + 1)
          .append(". ");

      appendDetailToStringBuilder(sb, result.get(i));
    }

    List<GameDetail> itemsToWrite = details.getKeysFromApi().stream()
        .filter(i -> details.getItems().containsKey(i))
        .map(i -> details.getItems().get(i))
        .collect(Collectors.toList());

    LOG.info("Writing games with id {} to db", details.getKeysFromApi());

    _dbClient.updateGames(itemsToWrite);

    String speechText = sb.toString();

    LOG.info("Speech text: {}", speechText);

    // Create the Simple card content.
    SimpleCard card = new SimpleCard();
    card.setTitle("Current Score");
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);

    PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
    outputSpeech.setText(
        "If you want more scores, you can say something like \"Give me the next 5 scores.\" To stop, say \"stop.\"");

    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(outputSpeech);

    return SpeechletResponse.newAskResponse(speech, reprompt, card);
  }

  private static void appendDetailToStringBuilder(StringBuilder sb, GameDetail gd) {
    sb.append(gd.getTeamA().getName())
        .append(String.format(" %s ", gd.getStatus() == MatchStatus.COMPLETE ? "played" : "is playing"))
        .append(gd.getTeamB().getName())
        .append(" at ")
        .append(gd.getShortVenue())
        .append(". ")
        .append(gd.getLiveStatus())
        .append(". ");
  }

  private SpeechletResponse getWelcomeResponse() {
    String speechText =
        "Welcome to Score Tracker, you can ask me what the score is by saying, \"what is the current score?\"";
    return newAskResponse(speechText, "Current Score");
  }

  /**
   * Creates a {@code SpeechletResponse} for the help intent.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getHelpResponse() {
    String speechText = "You can say give me an update on the games to me!";
    String title = "Current Score - Help";
    return newAskResponse(speechText, title);
  }

  private static SpeechletResponse newResponse(String text, String title, boolean isAsk) {
    // Create the Simple card content.
    SimpleCard card = new SimpleCard();
    card.setTitle(title);
    card.setContent(text);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(text);

    // Create reprompt
    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(speech);

    return isAsk ? SpeechletResponse.newAskResponse(speech, reprompt, card)
        : SpeechletResponse.newTellResponse(speech, card);
  }

  private static SpeechletResponse newAskResponse(String speechText, String title) {
    return newResponse(speechText, title, true);
  }

  private static SpeechletResponse newTellResponse(String text, String title) {
    return newResponse(text, title, false);
  }
}
