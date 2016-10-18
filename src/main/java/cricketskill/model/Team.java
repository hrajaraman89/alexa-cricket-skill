package cricketskill.model;

public class Team {
  private final String _name;
  private final int _id;

  public Team(String name, int id) {
    _name = name;
    _id = id;
  }

  public String getName() {
    return _name;
  }

  public int getId() {
    return _id;
  }
}
