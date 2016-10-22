package cricketskill.model;

import com.google.gson.Gson;


public class GameDetail {
  private int id;
  private int teamAId;
  private String teamAName;
  private int teamBId;
  private String teamBName;
  private String venue;
  private String shortVenue;
  private MatchStatus status;
  private String liveStatus;
  private int winnerId;
  private long lastUpdated;

  public int getId() {
    return id;
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

  public int getTeamBId() {
    return teamBId;
  }

  public String getTeamBName() {
    return teamBName;
  }

  public int getTeamAId() {
    return teamAId;
  }

  public String getTeamAName() {
    return teamAName;
  }
}
