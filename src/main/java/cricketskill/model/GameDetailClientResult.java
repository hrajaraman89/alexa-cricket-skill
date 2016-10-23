package cricketskill.model;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;


public class GameDetailClientResult {
  private final int _total;
  private final List<CricketGameDetail> _items;

  public GameDetailClientResult(int total, Map<Integer, CricketGameDetail> items) {
    this(total, Lists.newArrayList(items.values()));
  }

  public GameDetailClientResult(int total, List<CricketGameDetail> items) {
    _total = total;
    _items = items;
  }

  public List<CricketGameDetail> getItems() {
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
