package app.thetahatch.navi;
import org.junit.Test;import static org.junit.Assert.*;
public class FollowGuardTest {
 @Test public void allowsFiveCancelsPerWindowThenStops(){FollowGuard g=new FollowGuard();for(int i=0;i<FollowGuard.MAX_CANCELS;i++)assertTrue(g.allow(1000+i));assertFalse(g.allow(1100));assertEquals(FollowGuard.MAX_CANCELS,g.cancelled);assertEquals(1,g.refused);}
 @Test public void windowSlides(){FollowGuard g=new FollowGuard();for(int i=0;i<FollowGuard.MAX_CANCELS;i++)assertTrue(g.allow(i));assertFalse(g.allow(5000));assertTrue(g.allow(FollowGuard.WINDOW_MS+1));}
}
