package cricketskill.io;

import com.amazonaws.Protocol;


public class Stores {
  private final GameIdsStore _gameIdsStore;
  private final GameDetailStore _gameDetailStore;

  public Stores(Protocol protocol) {
    _gameIdsStore = new GameIdsStore(protocol);
    _gameDetailStore = new GameDetailStore(protocol);
  }

  public GameIdsStore getGameIdsStore() {
    return _gameIdsStore;
  }

  public GameDetailStore getGameDetailStore() {
    return _gameDetailStore;
  }
}
