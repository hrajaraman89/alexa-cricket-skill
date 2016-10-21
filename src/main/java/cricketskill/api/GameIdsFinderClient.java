package cricketskill.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cricketskill.common.UnsafeJsonOp;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import static cricketskill.common.OptionalUtils.isEmpty;
import static cricketskill.common.TrackerUtils.withTracking;
import static cricketskill.common.UnsafeJsonOp.safeJsonOp;


/**
 * Finds internation game ids that are currently ongoing
 */
public class GameIdsFinderClient {
  private static final Logger LOG = LoggerFactory.getLogger(GameIdsFinderClient.class);
  private static final String API_URL_TO_GET_IDS = "http://cricapi.com/api/cricket/";
  private static final String API_URL_TO_GET_IDS_2 = "http://www.espncricinfo.com/netstorage/summary.json";

  public List<Integer> getGameIds() {
    return withTracking(this::getGameIdsInternal, "Get Game Ids", LOG);
  }

  private List<Integer> getGameIdsInternal() {
    UnsafeJsonOp<JSONObject> getModules = () -> (JSONObject) new Resty().json(API_URL_TO_GET_IDS_2).get("modules");

    Optional<JSONObject> modulesOptional = safeJsonOp(getModules);

    if (isEmpty(modulesOptional)) {
      return Lists.newArrayList();
    }

    JSONObject modules = modulesOptional.get();

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

  private List<Integer> getGameIdsWithoutTracking() {

    UnsafeJsonOp<JSONArray> getArray = () -> (JSONArray) new Resty().json(API_URL_TO_GET_IDS).get("data");

    Optional<JSONArray> dataOptional = safeJsonOp(getArray);

    if (isEmpty(dataOptional)) {
      return Lists.newArrayList();
    }

    JSONArray data = dataOptional.get();

    List<Integer> gameIds = IntStream.range(0, data.length()).boxed()
        .map(d -> safeJsonOp(() -> data.getJSONObject(d)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(dataItem -> safeJsonOp(() -> dataItem.getInt("unique_id")))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    LOG.info("Returning ids: {}", gameIds);

    return gameIds;
  }
}
