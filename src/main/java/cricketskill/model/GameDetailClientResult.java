package cricketskill.model;

import com.google.gson.GsonBuilder;
import java.util.Map;
import java.util.Set;


public class GameDetailClientResult {
  private final Map<Integer, GameDetail> _items;
  private final Set<Integer> _keysFromCache;
  private final Set<Integer> _keysFromApi;

  public GameDetailClientResult(Map<Integer, GameDetail> items, Set<Integer> keysFromCache, Set<Integer> keysFromApi) {
    _items = items;
    _keysFromCache = keysFromCache;
    _keysFromApi = keysFromApi;
  }

  public Map<Integer, GameDetail> getItems() {
    return _items;
  }

  public Set<Integer> getKeysFromCache() {
    return _keysFromCache;
  }

  public Set<Integer> getKeysFromApi() {
    return _keysFromApi;
  }

  public String toString() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .create()
        .toJson(this);
  }
}
