package cricketskill.model;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;


public class GameDetailClientResult {
  private int _total;
  private final List<GameDetail> _items;

  public GameDetailClientResult(int total, Map<Integer, GameDetail> items) {
    this(total, Lists.newArrayList(items.values()));
  }

  public GameDetailClientResult(int total, List<GameDetail> items) {
    _total = total;
    _items = items;
  }

  public List<GameDetail> getItems() {
    return _items;
  }

  public void addTotal(int total) {
    _total += total;
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
