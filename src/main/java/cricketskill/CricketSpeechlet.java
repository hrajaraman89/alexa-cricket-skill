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
import com.google.common.collect.Sets;
import cricketskill.api.GameDetailClient;
import cricketskill.common.TrackerUtils;
import cricketskill.io.Stores;
import cricketskill.model.CricketGameDetail;
import cricketskill.model.GameDetailClientResult;
import cricketskill.model.MatchStatus;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CricketSpeechlet implements Speechlet {
  private static final Logger LOG = LoggerFactory.getLogger(CricketSpeechlet.class);

  private final GameDetailClient _client;
  private final Stores _stores;

  private final Map<String, BiFunction<Intent, Session, SpeechletResponse>> _intentToHandler = ImmutableMap.of(
      "CurrentScoreIntent",
      (i, s) -> handleCurrentScoreIntent(s, 1),
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
  public void onSessionEnded(final SessionEndedRequest request, final Session session)
      throws SpeechletException {
    LOG.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
        session.getSessionId());
    // any cleanup logic goes here
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

    String filtered = Arrays.stream(slotValue.split(" "))
        .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
        .collect(Collectors.joining(" "));

    try {
      _stores.getFavoriteTeamStore().addFavoriteTeam(session.getUser().getUserId(), filtered);
    } catch (Throwable t) {
      LOG.error("Unable to add favorite {}", t.getMessage());
    }

    return newTellResponse("You want to add " + filtered + " as a favorite.", "Score Tracker");
  }

  private SpeechletResponse handleEndIntent() {
    return newTellResponse("Okay, ending.", "Ending score tracker");
  }

  private SpeechletResponse nextScoreIntent(Intent intent, Session session) {
    Slot slot = intent.getSlot("NumberOfGames");

    int count = Optional.ofNullable(slot.getValue())
        .map(Integer::valueOf)
        .orElse(1);

    LOG.info("Slot value from number of games is {}. Count is {}", slot.getValue(), count);

    return handleCurrentScoreIntent(session, count);
  }

  SpeechletResponse handleCurrentScoreIntent(Session session, int count) {
    return TrackerUtils.withTracking(() -> getCurrentScoreResponseInternal(session, count), "Get current score", LOG);
  }

  private SpeechletResponse getCurrentScoreResponseInternal(Session session, int count) {

    LOG.info("Getting current response");

    @SuppressWarnings("unchecked")
    Collection<Integer> seenGameIds = (Collection<Integer>) session.getAttributes()
        .getOrDefault("seenGameIds", Sets.newHashSet());

    LOG.info("Game ids {} has already been seen", seenGameIds);

    Set<Integer> seen = seenGameIds.stream().collect(Collectors.toSet());

    GameDetailClientResult gameDetailClientResult = getNext(count, seen, session.getUser().getUserId());
    List<CricketGameDetail> items = gameDetailClientResult.getItems();

    if (items.isEmpty()) {
      return newTellResponse("There are no more current games", "Score Tracker");
    }

    items.stream()
        .map(CricketGameDetail::getId)
        .forEach(seen::add);

    session.setAttribute("seenGameIds", seen);

    LOG.info("New seen games is {}", seen);

    StringBuilder sb = new StringBuilder();

    if (seenGameIds.isEmpty()) {
      sb.append(String.format("There are a total of %d games. ", gameDetailClientResult.getTotal()));
    }

    items.forEach(i -> appendDetailToStringBuilder(sb, i));

    boolean reachedEndOfUpdate = seen.size() == gameDetailClientResult.getTotal();

    if (reachedEndOfUpdate) {
      sb.append(" This is the last game.");
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
    outputSpeech.setText("Would you like to hear the next match? You can say \"Yes\" or \"No\".");

    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(outputSpeech);

    return reachedEndOfUpdate ? SpeechletResponse.newTellResponse(speech, card)
        : SpeechletResponse.newAskResponse(speech, reprompt, card);
  }

  private GameDetailClientResult getNext(int count, Set<Integer> seen, String userId) {

    GameDetailClientResult result = _client.getDetails();

    final Set<String> teams = _stores.getFavoriteTeamStore().getFavoriteTeams(userId);

    List<CricketGameDetail> items = result.getItems();

    List<CricketGameDetail> unseen = items.stream()
        .filter(i -> !seen.contains(i.getId()))
        .sorted((o1, o2) -> isFavoriteTeamPlaying(o1, teams) ? -1 : 1)
        .collect(Collectors.toList());

    LOG.info("unseen game ids {}", unseen.stream().map(CricketGameDetail::getId).collect(Collectors.toList()));

    return new GameDetailClientResult(result.getTotal(), unseen.subList(0, Math.min(count, unseen.size())));
  }

  private static boolean isFavoriteTeamPlaying(CricketGameDetail gd, Set<String> teams) {
    return Stream.of(gd.getTeamAName(), gd.getTeamBName())
        .anyMatch(teams::contains);
  }

  private static void appendDetailToStringBuilder(StringBuilder sb, CricketGameDetail gd) {
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
    return newAskResponse(speechText, "Score Tracker");
  }

  /**
   * Creates a {@code SpeechletResponse} for the help intent.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse handleHelpIntent() {
    String speechText = "You can say give me an update on the games to me!";
    return newAskResponse(speechText, "Score Tracker - Help");
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
