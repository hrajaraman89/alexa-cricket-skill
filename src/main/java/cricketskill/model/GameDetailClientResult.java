package cricketskill.model;

import com.google.gson.GsonBuilder;
import java.util.Map;


public class GameDetailClientResult {
  private final int _total;
  private final Map<Integer, GameDetail> _items;

  public GameDetailClientResult(int total, Map<Integer, GameDetail> items) {
    _total = total;
    _items = items;
  }

  public Map<Integer, GameDetail> getItems() {
    return _items;
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
