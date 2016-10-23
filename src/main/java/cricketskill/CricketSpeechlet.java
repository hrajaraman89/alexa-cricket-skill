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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cricketskill.api.GameDetailClient;
import cricketskill.common.TrackerUtils;
import cricketskill.io.Stores;
import cricketskill.model.GameDetail;
import cricketskill.model.GameDetailClientResult;
import cricketskill.model.MatchStatus;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CricketSpeechlet implements Speechlet {
  private static final Logger LOG = LoggerFactory.getLogger(CricketSpeechlet.class);
  private static final int PAGE_LENGTH = 3;
  static final String START_KEY = "start";

  private final GameDetailClient _client;
  private final Stores _stores;

  private final Map<String, BiFunction<Intent, Session, SpeechletResponse>> _intentToHandler = ImmutableMap.of(
      "CurrentScoreIntent",
      (i, s) -> handleCurrentScoreIntent(s),
      "AMAZON.HelpIntent",
      (i, s) -> handleHelpIntent(),
      "NextScoreIntent",
      this::nextScoreIntent,
      "EndIntent",
      (i, s) -> handleEndIntent(),
      "AddFavoriteIntent",
      this::handleAddFavoriteIntent
  );

  public CricketSpeechlet() {
    this(Protocol.HTTP);
  }

  public CricketSpeechlet(Protocol protocol) {
    _stores = new Stores(protocol);
    _client = new GameDetailClient(_stores);
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

    Supplier<SpeechletException> onError = () -> new SpeechletException("Intent name is null");

    Intent intent = request.getIntent();

    return Optional.ofNullable(intent)
        .map(Intent::getName)
        .filter(_intentToHandler::containsKey)
        .map(_intentToHandler::get)
        .map(f -> f.apply(intent, session))
        .orElseThrow(onError);
  }

  private SpeechletResponse handleAddFavoriteIntent(Intent intent, Session session) {

    Slot slot = intent.getSlot("FavoriteCountry");

    String slotValue = slot.getValue();

    LOG.info("{} wants to add {}", session.getUser().getUserId(), slotValue);

    if (!Character.isUpperCase(slotValue.charAt(0))) {
      return newTellResponse("Sorry, we're having trouble adding that country.", "Score Tracker");
    }

    try {
      _stores.getFavoriteTeamStore().addFavoriteTeam(session.getUser().getUserId(), slotValue);
    } catch (Throwable t) {
      LOG.error("Unable to add favorite {}", t.getMessage());
    }

    return newTellResponse("You want to add " + slotValue + " as a favorite.", "Score Tracker");
  }

  private SpeechletResponse handleEndIntent() {
    return newTellResponse("Okay, ending.", "Ending score tracker");
  }

  private SpeechletResponse nextScoreIntent(Intent intent, Session session) {
    Slot slot = intent.getSlot("NumberOfGames");

    LOG.info("Slot value from number of games is {}", slot.getValue());

    int count = Integer.valueOf(slot.getValue());
    return handleCurrentScoreIntent(session, count);
  }

  @Override
  public void onSessionEnded(final SessionEndedRequest request, final Session session)
      throws SpeechletException {
    LOG.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());
    // any cleanup logic goes here
  }

  public SpeechletResponse handleCurrentScoreIntent(Session session) {
    return handleCurrentScoreIntent(session, PAGE_LENGTH);
  }

  private SpeechletResponse handleCurrentScoreIntent(Session session, int count) {
    return TrackerUtils.withTracking(() -> getCurrentScoreResponseInternal(session, count), "Get current score", LOG);
  }

  private SpeechletResponse getCurrentScoreResponseInternal(Session session, int count) {

    LOG.info("Getting current response");

    int start = Optional.ofNullable(session.getAttribute(START_KEY))
        .map(Object::toString)
        .map(Integer::valueOf)
        .orElse(0);

    LOG.info("Session's start is {}", start);

    GameDetailClientResult result = new GameDetailClientResult(0, Lists.newArrayList());

    if (start == 0) {
      result = getFromFavorites(session);
    }

    if (result.getTotal() < count) {

      count -= result.getTotal();

      LOG.info("Fetching {} non-favorite items", count);

      @SuppressWarnings("unchecked")
      Collection<Integer> seenGameIds = (Collection<Integer>) session.getAttributes()
          .getOrDefault("seenGameIds", Sets.newHashSet());

      Set<Integer> seen = seenGameIds.stream().collect(Collectors.toSet());

      GameDetailClientResult newResult = _client.getDetails(start, count, seen);

      result.getItems().addAll(newResult.getItems());
      result.addTotal(newResult.getTotal());
    }

    List<GameDetail> items = result.getItems();

    if (items.isEmpty()) {
      return newTellResponse("There are no more current games", "Score Tracker");
    }

    session.setAttribute(START_KEY, (start + items.size()));

    LOG.info("Session's new start is {}", session.getAttribute(START_KEY));

    StringBuilder sb = new StringBuilder();

    sb.append("Giving updates on ");

    if (start == 0) {
      sb.append(String.format("first %d matches. ", items.size()));
    } else if (start + items.size() < result.getTotal()) {
      sb.append(String.format("matches %d to %d. ", start + 1, (start + items.size())));
    } else {
      sb.append(String.format("last %d matches. ", items.size()));
    }

    for (int i = 0; i < items.size(); i++) {
      sb.append(" ")
          .append(i + start + 1)
          .append(". ");

      appendDetailToStringBuilder(sb, items.get(i));
    }

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

  private GameDetailClientResult getFromFavorites(Session session) {

    //get favorite ids
    //get favorite games
    //add them to 'seen' so that they don't repeat

    Set<String> favorites = _stores.getFavoriteTeamStore().getFavoriteTeams(session.getUser().getUserId());

    List<GameDetail> items = _stores.getGameDetailStore().getGamesByTeam(favorites);

    GameDetailClientResult result = new GameDetailClientResult(items.size(), items);

    Set<Integer> gameIds = items.stream().map(GameDetail::getId).collect(Collectors.toSet());

    LOG.info("Games from favorites add as 'seen' {}", gameIds);

    session.setAttribute("seenGameIds", gameIds);
    return result;
  }

  private static void appendDetailToStringBuilder(StringBuilder sb, GameDetail gd) {
    sb.append(gd.getTeamAName())
        .append(String.format(" %s ", gd.getStatusEnum() == MatchStatus.COMPLETE ? "played" : "is playing"))
        .append(gd.getTeamBName())
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
  private SpeechletResponse handleHelpIntent() {
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
