package cricketskill.model;

import com.google.gson.Gson;

@SuppressWarnings({"unused", "checkstyle"})
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

  private GameDetail setStatus(MatchStatus status) {
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

  public int getWinnerId() {
    return winnerId;
  }

  public long getLastUpdated() {
    return lastUpdated;
  }

  public String getShortVenue() {
    return shortVenue;
  }

  public boolean isInternational() {
    return isInternational;
  }
}
