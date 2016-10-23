package cricketskill.io;

import com.amazonaws.Protocol;


public class Stores {
  private final GameIdsStore _gameIdsStore;
  private final GameDetailStore _gameDetailStore;
  private final FavoriteTeamStore _favoriteTeamStore;

  public Stores(Protocol protocol) {
    _gameIdsStore = new GameIdsStore(protocol);
    _gameDetailStore = new GameDetailStore(protocol);
    _favoriteTeamStore = new FavoriteTeamStore(protocol);
  }

  public GameIdsStore getGameIdsStore() {
    return _gameIdsStore;
  }

  public GameDetailStore getGameDetailStore() {
    return _gameDetailStore;
  }

  public FavoriteTeamStore getFavoriteTeamStore() {
    return _favoriteTeamStore;
  }
}
