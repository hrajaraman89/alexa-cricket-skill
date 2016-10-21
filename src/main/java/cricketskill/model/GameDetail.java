package cricketskill.model;

import com.google.gson.Gson;


public class GameDetail {
  private int id;
  private Team teamA;
  private Team teamB;
  private String venue;
  private MatchStatus status;
  private String liveStatus;
  private int winnerId;
  private boolean isCachedResult;

  public GameDetail setId(int id) {
    this.id = id;
    return this;
  }

  public GameDetail setTeamA(Team teamA) {
    this.teamA = teamA;
    return this;
  }

  public GameDetail setTeamB(Team teamB) {
    this.teamB = teamB;
    return this;
  }

  public GameDetail setVenue(String venue) {
    this.venue = venue;
    return this;
  }

  public GameDetail setWinnerId(int winnerId) {
    this.winnerId = winnerId;
    return this;
  }

  public int getId() {
    return id;
  }

  public Team getTeamA() {
    return teamA;
  }

  public Team getTeamB() {
    return teamB;
  }

  public String getVenue() {
    return venue;
  }

  public String toString() {
    return new Gson().toJson(this);
  }

  public GameDetail setStatus(MatchStatus status) {
    this.status = status;
    return this;
  }

  public MatchStatus getStatus() {

    if (getStatus() == MatchStatus.CURRENT && winnerId != 0) {
      setStatus(MatchStatus.COMPLETE);
    }

    return status;
  }

  public String getLiveStatus() {
    return liveStatus;
  }

  public GameDetail setLiveStatus(String liveStatus) {
    this.liveStatus = liveStatus;
    return this;
  }

  public int getWinnerId() {
    return winnerId;
  }

  public boolean isCachedResult() {
    return isCachedResult;
  }

  public GameDetail setCachedResult(boolean cachedResult) {
    isCachedResult = cachedResult;
    return this;
  }
}
