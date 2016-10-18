package cricketskill.model;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Optional;


public class GameDetail {
  private int _id;
  private Team _teamA;
  private Team _teamB;
  private String _venue;
  private Optional<Team> _winner;
  private MatchStatus _status;
  private String _liveStatus;

  public GameDetail setId(int id) {
    _id = id;
    return this;
  }

  public GameDetail setTeamA(Team teamA) {
    _teamA = teamA;
    return this;
  }

  public GameDetail setTeamB(Team teamB) {
    _teamB = teamB;
    return this;
  }

  public GameDetail setVenue(String venue) {
    _venue = venue;
    return this;
  }

  public GameDetail setWinnerId(int winnerId) {
    _winner = Optional.of(winnerId)
        .filter(i -> i != 0)
        .flatMap(wId -> Arrays.asList(_teamA, _teamB)
            .stream()
            .filter(t -> t.getId() == wId)
            .findFirst());

    return this;
  }

  public int getId() {
    return _id;
  }

  public Team getTeamA() {
    return _teamA;
  }

  public Team getTeamB() {
    return _teamB;
  }

  public String getVenue() {
    return _venue;
  }

  public String toString() {
    return new Gson().toJson(this);
  }

  public Optional<Team> getWinner() {
    return _winner;
  }

  public GameDetail setStatus(MatchStatus status) {
    _status = status;
    return this;
  }

  public MatchStatus getStatus() {
    return _status;
  }

  public String getLiveStatus() {
    return _liveStatus;
  }

  public GameDetail setLiveStatus(String liveStatus) {
    _liveStatus = liveStatus;
    return this;
  }
}
