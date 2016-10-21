package cricketskill.model;

import com.google.gson.GsonBuilder;
import java.util.Map;
import java.util.Set;


public class GameDetailClientResult {
  private final int _total;
  private final Map<Integer, GameDetail> _items;
  private final Set<Integer> _keysFromApi;

  public GameDetailClientResult(int total, Map<Integer, GameDetail> items, Set<Integer> keysFromApi) {
    _total = total;
    _items = items;
    _keysFromApi = keysFromApi;
  }

  public Map<Integer, GameDetail> getItems() {
    return _items;
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

  public int getTotal() {
    return _total;
  }
}
