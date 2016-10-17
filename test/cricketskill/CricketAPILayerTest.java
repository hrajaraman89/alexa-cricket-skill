package cricketskill;

import static org.junit.Assert.*;

/**
 * Created by rasriram on 10/16/16.
 */
public class CricketAPILayerTest {
    @org.junit.Test
    public void getCurrentMatches() throws Exception {
        CricketAPILayer$.MODULE$.getCricketMatches();
    }
}