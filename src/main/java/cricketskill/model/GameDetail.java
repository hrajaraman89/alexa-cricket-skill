package cricketskill.model;

import com.google.gson.Gson;


public class GameDetail {
  private int id;
  private Team teamA;
  private Team teamB;
  private String venue;
  private String shortVenue;
  private MatchStatus status;
  private String liveStatus;
  private int winnerId;
  private long lastUpdated;
  private boolean isInternational;

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

    if (status == MatchStatus.CURRENT && winnerId != 0) {
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

  public long getLastUpdated() {
    return lastUpdated;
  }

  public GameDetail setLastUpdated(long lastUpdated) {
    this.lastUpdated = lastUpdated;
    return this;
  }

  public String getShortVenue() {
    return shortVenue;
  }

  public GameDetail setShortVenue(String shortVenue) {
    this.shortVenue = shortVenue;
    return this;
  }

  public boolean isInternational() {
    return isInternational;
  }

  public GameDetail setInternational(boolean international) {
    isInternational = international;
    return this;
  }
}
