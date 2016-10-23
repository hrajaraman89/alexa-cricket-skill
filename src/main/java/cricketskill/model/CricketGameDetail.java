package cricketskill.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.google.gson.Gson;


@SuppressWarnings({"CheckStyle"})
@DynamoDBTable(tableName = "CricketGameDetail")
public class CricketGameDetail {
  private int id;
  private int teamAId;
  private String teamAName;
  private int teamBId;
  private String teamBName;
  private String venue;
  private String shortVenue;
  private String status;
  private String liveStatus;
  private int winnerId;
  private long lastUpdated;
  private int battingTeamId;
  private int bowlingTeamId;
  private int runs;
  private int target;
  private double runRate;
  private int wickets;
  private String overs;

  @DynamoDBAttribute(attributeName = "id")
  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public String getBowlingTeamName() {
    return getNameForId(bowlingTeamId);
  }

  public String getBattingTeamName() {
    return getNameForId(battingTeamId);
  }

  private String getNameForId(int id) {
    return id == teamAId ? teamAName : teamBName;
  }

  @DynamoDBAttribute(attributeName = "teamAId")
  public void setTeamAId(int teamAId) {
    this.teamAId = teamAId;
  }

  @DynamoDBAttribute(attributeName = "teamAName")
  public void setTeamAName(String teamAName) {
    this.teamAName = teamAName;
  }

  public String getTeamAName() {
    return teamAName;
  }

  @DynamoDBAttribute(attributeName = "teamBId")
  public int getTeamBId() {
    return teamBId;
  }

  public void setTeamBId(int teamBId) {
    this.teamBId = teamBId;
  }

  @DynamoDBAttribute(attributeName = "teamBName")
  public void setTeamBName(String teamBName) {
    this.teamBName = teamBName;
  }

  public String getTeamBName() {
    return teamBName;
  }

  @DynamoDBAttribute(attributeName = "venue")
  public String getVenue() {
    return venue;
  }

  public void setVenue(String venue) {
    this.venue = venue;
  }

  @DynamoDBAttribute(attributeName = "shortVenue")
  public String getShortVenue() {
    return shortVenue;
  }

  public void setShortVenue(String shortVenue) {
    this.shortVenue = shortVenue;
  }

  @DynamoDBAttribute(attributeName = "status")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public MatchStatus getStatusEnum() {
    return MatchStatus.valueOf(getStatus());
  }

  @DynamoDBAttribute(attributeName = "liveStatus")
  public String getLiveStatus() {
    return liveStatus;
  }

  public void setLiveStatus(String liveStatus) {
    this.liveStatus = liveStatus;
  }

  @DynamoDBAttribute(attributeName = "winnerId")

  public int getWinnerId() {
    return winnerId;
  }

  public void setWinnerId(int winnerId) {
    this.winnerId = winnerId;
  }

  @DynamoDBAttribute(attributeName = "lastUpdated")
  public long getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(long lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @DynamoDBAttribute(attributeName = "battingTeamid")
  public void setBattingTeamId(int battingTeamId) {
    this.battingTeamId = battingTeamId;
  }

  public int getBattingTeamId() {
    return this.battingTeamId;
  }

  @DynamoDBAttribute(attributeName = "bowlingTeamId")
  public void setBowlingTeamId(int bowlingTeamId) {
    this.bowlingTeamId = bowlingTeamId;
  }

  public int getBowlingTeamId() {
    return this.bowlingTeamId;
  }

  @DynamoDBAttribute(attributeName = "runs")
  public int getRuns() {
    return runs;
  }

  public void setRuns(int runs) {
    this.runs = runs;
  }

  @DynamoDBAttribute(attributeName = "target")
  public int getTarget() {
    return target;
  }

  public void setTarget(int target) {
    this.target = target;
  }

  @DynamoDBAttribute(attributeName = "runRate")

  public double getRunRate() {
    return runRate;
  }

  public void setRunRate(double runRate) {
    this.runRate = runRate;
  }

  @DynamoDBAttribute(attributeName = "wickets")
  public int getWickets() {
    return wickets;
  }

  public void setWickets(int wickets) {
    this.wickets = wickets;
  }

  @DynamoDBAttribute(attributeName = "overs")
  public String getOvers() {
    return overs;
  }

  public void setOvers(String overs) {
    this.overs = overs;
  }

  public String toString() {
    return new Gson().toJson(this);
  }
}
