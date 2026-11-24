package com.theta.navihatch;
import org.junit.Test;import static org.junit.Assert.*;
public class CameraLinkTest {
 private CameraState pose(){return new CameraState(35.681,139.767,17,42,30);}
 @Test public void openCopiesPoseWithoutMovingCar(){CameraLink l=new CameraLink();int[] carCalls={0};l.attach(true,c->carCalls[0]++);l.changed(true,pose());int before=carCalls[0];l.openCarOnTablet();assertTrue(l.tablet.near(pose()));assertEquals(before,carCalls[0]);}
 @Test public void independentTabletDoesNotMoveCar(){CameraLink l=new CameraLink();CameraState initial=l.car;l.changed(false,pose());assertTrue(l.car.near(initial));assertTrue(l.tablet.near(pose()));}
 @Test public void linkedCallbacksDoNotEchoForever(){CameraLink l=new CameraLink();int[] moves={0};l.attach(true,c->{moves[0]++;l.changed(true,c);});l.attach(false,c->{moves[0]++;l.changed(false,c);});l.setLinked(true);int before=moves[0];l.changed(false,pose());assertTrue(l.car.near(pose()));assertEquals(before+1,moves[0]);}
 @Test public void closingTabletDoesNotDetachCar(){CameraLink l=new CameraLink();int[] carCalls={0},tabletCalls={0};l.attach(true,c->carCalls[0]++);l.attach(false,c->tabletCalls[0]++);l.detach(false);l.set(true,pose());assertEquals(2,carCalls[0]);assertEquals(1,tabletCalls[0]);assertTrue(l.car.near(pose()));}
 @Test public void reopeningUsesCurrentCarPose(){CameraLink l=new CameraLink();l.detach(false);l.changed(true,pose());l.openCarOnTablet();CameraState[] actual={null};l.attach(false,c->actual[0]=c);assertTrue(actual[0].near(pose()));}
 @Test(expected=IllegalArgumentException.class) public void invalidCoordinatesRejected(){new CameraState(Double.NaN,135,15,0,0);}
}
