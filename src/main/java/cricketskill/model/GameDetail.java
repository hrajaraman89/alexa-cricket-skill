package cricketskill.model;

public class GameDetail {
  public int id;
  public int teamAId;
  public String teamAName;
  public int teamBId;
  public String teamBName;
  public String venue;
  public String shortVenue;
  public MatchStatus status;
  public String liveStatus;
  public int winnerId;
  public long lastUpdated;
  public int battingTeamId;
  public int bowlingTeamId;
  public int runs;
  public int target;
  public double runRate;
  public int wickets;
  public String overs;

  public String getBowlingTeamName() {
    return getNameForId(bowlingTeamId);
  }

  public String getBattingTeamName() {
    return getNameForId(battingTeamId);
  }

  private String getNameForId(int id) {
    return id == teamAId ? teamAName : teamBName;
  }
}
