package com.theta.navihatch;
import android.location.Location;
import androidx.car.app.hardware.common.CarValue;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class LocationProbeTest {
 private Location fix(){Location l=new Location("test-provider");l.setLatitude(34.6);l.setLongitude(135.5);l.setTime(100000);l.setAccuracy(4);return l;}
 @Test public void successfulCallbackKeepsMetadataWithoutClaimingOrigin()throws Exception{LocationProbeState s=new LocationProbeState("car");s.accept(CarValue.STATUS_SUCCESS,900,fix(),1000,100000);assertEquals("RECENT",s.freshness(1500,100500));assertEquals(1,s.validSamples);assertFalse(s.json(1500,100500).getBoolean("physical_sensor_origin_verified"));assertEquals(4,s.json(1500,100500).getJSONObject("location").getDouble("accuracy_m"),.01);}
 @Test public void errorClearsPriorLocation(){LocationProbeState s=new LocationProbeState("car");s.accept(CarValue.STATUS_SUCCESS,900,fix(),1000,100000);s.accept(CarValue.STATUS_UNAVAILABLE,1001,null,1001,100001);assertEquals("UNAVAILABLE",s.status);assertNull(s.latest);assertEquals(1,s.validSamples);assertEquals(2,s.callbacks);}
 @Test public void successWithoutFixAndUnimplementedAreDistinct(){LocationProbeState s=new LocationProbeState("car");s.accept(CarValue.STATUS_SUCCESS,0,null,1000,100000);assertEquals("SUCCESS_WITHOUT_LOCATION",s.status);s.accept(CarValue.STATUS_UNIMPLEMENTED,0,null,1001,100001);assertEquals("UNIMPLEMENTED",s.status);assertEquals(0,s.validSamples);}
 @Test public void staleAndUnknownTimestampsAreNotRecent(){LocationProbeState s=new LocationProbeState("car");s.accept(CarValue.STATUS_SUCCESS,900,fix(),1000,100000);assertEquals("STALE_CALLBACK",s.freshness(17000,116000));Location old=fix();old.setTime(1);s.accept(CarValue.STATUS_SUCCESS,900,old,1000,100000);assertEquals("STALE_FIX",s.freshness(1001,100001));old.setTime(0);s.accept(CarValue.STATUS_SUCCESS,900,old,1000,100000);assertEquals("FIX_TIME_UNKNOWN",s.freshness(1001,100001));}
 @Test public void staleCarTimestampIsNotFreshDespiteNewCallback(){LocationProbeState s=new LocationProbeState("car");s.accept(CarValue.STATUS_SUCCESS,1,fix(),20000,100000);assertEquals("STALE_CAR_VALUE",s.freshness(20000,100000));}
 @Test public void noHostDoesNotFabricateLocationAndDisableStopsProbe()throws Exception{NaviApp app=(NaviApp)RuntimeEnvironment.getApplication();try{app.vehicleProbe.setEnabled(true);fail();}catch(IllegalStateException expected){}app.command("test","enable",1,0,"");app.command("test","vehicle_probe",1,0,"");assertEquals("WAITING_FOR_HOST",app.vehicleProbe.car.status);assertFalse(app.vehicleProbe.state().getBoolean("comparison_eligible"));assertFalse(app.vehicleProbe.state().getBoolean("navigation_location_injected"));app.command("test","disable",0,0,"");assertFalse(app.vehicleProbe.enabled);}
 @Test public void futureCarClockCannotBeCompared(){LocationProbeState s=new LocationProbeState("car");s.accept(CarValue.STATUS_SUCCESS,100000,fix(),1000,100000);assertEquals("CAR_CLOCK_UNKNOWN",s.freshness(1001,100001));}
 @Test public void nonFiniteAccuracyIsNullAndMissingFieldsStayNull()throws Exception{LocationProbeState s=new LocationProbeState("car");Location l=fix();l.setAccuracy(Float.NaN);s.accept(CarValue.STATUS_SUCCESS,900,l,1000,100000);org.json.JSONObject p=s.json(1000,100000).getJSONObject("location");assertTrue(p.isNull("accuracy_m"));assertTrue(p.isNull("speed_mps"));}
 @Test public void oldHostApiIsReportedWithoutCallingHardware(){NaviApp app=(NaviApp)RuntimeEnvironment.getApplication();androidx.car.app.testing.TestCarContext host=androidx.car.app.testing.TestCarContext.createCarContext(app);host.updateHandshakeInfo(new androidx.car.app.HandshakeInfo("test",2));Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION);app.vehicleProbe.attach(host);app.testMode=true;app.vehicleProbe.setEnabled(true);assertEquals("HOST_API_BELOW_3",app.vehicleProbe.car.status);app.vehicleProbe.detach(host);assertEquals("DISCONNECTED",app.vehicleProbe.car.status);}
}
