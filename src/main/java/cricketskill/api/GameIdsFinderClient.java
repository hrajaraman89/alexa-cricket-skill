package cricketskill.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cricketskill.common.UnsafeJsonOp;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import static cricketskill.common.TrackerUtils.withTracking;
import static cricketskill.common.UnsafeJsonOp.safeJsonOp;


/**
 * Finds internation game ids that are currently ongoing
 */
public class GameIdsFinderClient {
  private static final Logger LOG = LoggerFactory.getLogger(GameIdsFinderClient.class);
  private static final String API_URL_TO_GET_IDS_2 = "http://www.espncricinfo.com/netstorage/summary.json";

  public List<Integer> getGameIds() {
    return withTracking(this::getGameIdsInternal, "Get Game Ids", LOG);
  }

  private List<Integer> getGameIdsInternal() {
    Optional<JSONResource> json = safeJsonOp(() -> new Resty().json(API_URL_TO_GET_IDS_2));

    List<Integer> internalGameIds = json.map(getJSONForKey("modules"))
        .flatMap(UnsafeJsonOp::safeJsonOp)
        .map(GameIdsFinderClient::getInternalGameIds)
        .orElse(Lists.newArrayList());

    if (internalGameIds.isEmpty()) {
      return Lists.newArrayList();
    }

    return toExternalGameIds(json.get(), internalGameIds);
  }

  private static List<Integer> toExternalGameIds(JSONResource json, List<Integer> internalGameIds) {
    return safeJsonOp(getJSONForKey("matches").apply(json))
        .map(matches -> toExternalGameIds(matches, internalGameIds))
        .orElse(Lists.newArrayList());
  }

  private static List<Integer> toExternalGameIds(JSONObject matches, List<Integer> internalGameIds) {
    return internalGameIds.stream()
        .map(String::valueOf)
        .map(matches::optJSONObject)
        .filter(match -> match != null)
        .map(match -> match.optString("url"))
        .filter(url -> url != null)
        .map(GameIdsFinderClient::toExternalId)
        .collect(Collectors.toList());
  }

  //url: "/sunfoil-series-2016-17/engine/match/1003571.html"
  private static Integer toExternalId(String url) {
    int lastSlash = url.lastIndexOf('/');
    int lastPeriod = url.lastIndexOf('.');

    String idAsString = url.substring(lastSlash + 1, lastPeriod);

    return Integer.valueOf(idAsString);
  }

  private static Function<JSONResource, UnsafeJsonOp<JSONObject>> getJSONForKey(String key) {
    return j -> () -> (JSONObject) j.get(key);
  }

  private static List<Integer> getInternalGameIds(JSONObject modules) {
    Iterator<String> keys = modules.keys();

    Set<Integer> ids = Sets.newHashSet();

    while (keys.hasNext()) {
      String key = keys.next();

      LOG.info("Inspecting key={} within modules", key);

      JSONArray module = safeJsonOp(() -> modules.getJSONArray(key)).get();

      for (int i = 0; i < module.length(); i++) {
        final int j = i;
        JSONObject subModule = safeJsonOp(() -> module.getJSONObject(j)).get();

        String category = safeJsonOp(() -> subModule.getString("category")).get();

        LOG.info("Found submodule with category={}", category);

        if (category.equals("intl")) {
          JSONArray items = safeJsonOp(() -> subModule.getJSONArray("matches")).get();

          IntStream.range(0, items.length()).boxed()
              .map(items::optInt)
              .forEach(ids::add);

          LOG.info("Found intl. Moving to next key");

          break;
        }
      }
    }

    return Lists.newArrayList(ids);
  }
}
