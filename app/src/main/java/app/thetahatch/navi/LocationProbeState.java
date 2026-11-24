package app.thetahatch.navi;

import android.location.Location;
import androidx.car.app.hardware.common.CarValue;
import org.json.JSONException;
import org.json.JSONObject;

/** Observable API result, not a claim about the physical sensor behind the host. */
public final class LocationProbeState {
 public final String source;
 public String status="NOT_STARTED";
 public int statusCode=-1, callbacks, validSamples;
 public long receivedElapsed=-1, receivedWall, carTimestamp=-1;
 public Location latest;
 public LocationProbeState(String source){this.source=source;}
 public void reset(String status){this.status=status;statusCode=-1;callbacks=0;validSamples=0;receivedElapsed=-1;receivedWall=0;carTimestamp=-1;latest=null;}
 public void accept(int code,long timestamp,Location location,long elapsed,long wall){
  callbacks++;statusCode=code;receivedElapsed=elapsed;receivedWall=wall;carTimestamp=timestamp;latest=null;
  switch(code){case CarValue.STATUS_SUCCESS:status="SUCCESS";break;case CarValue.STATUS_UNIMPLEMENTED:status="UNIMPLEMENTED";break;case CarValue.STATUS_UNAVAILABLE:status="UNAVAILABLE";break;default:status="UNKNOWN";}
  if(code!=CarValue.STATUS_SUCCESS)return;
  if(location==null){status="SUCCESS_WITHOUT_LOCATION";return;}
  if(!Double.isFinite(location.getLatitude())||!Double.isFinite(location.getLongitude())||Math.abs(location.getLatitude())>90||Math.abs(location.getLongitude())>180){status="INVALID_COORDINATES";return;}
  latest=new Location(location);validSamples++;
 }
 public String freshness(long elapsed,long wall){
  if(latest==null)return "NO_VALID_FIX";
  if(receivedElapsed<0||elapsed<receivedElapsed)return "CLOCK_UNKNOWN";
  if(elapsed-receivedElapsed>15000)return "STALE_CALLBACK";
  if(latest.getTime()<=0||wall<latest.getTime()-5000)return "FIX_TIME_UNKNOWN";
  if(wall-latest.getTime()>15000)return "STALE_FIX";
  if(carTimestamp>elapsed+5000)return "CAR_CLOCK_UNKNOWN";
  if(carTimestamp>0&&carTimestamp<=elapsed&&elapsed-carTimestamp>15000)return "STALE_CAR_VALUE";
  return "RECENT";
 }
 private static Object finite(double value){return Double.isFinite(value)?value:JSONObject.NULL;}
 public JSONObject json(long elapsed,long wall)throws JSONException{
  JSONObject j=new JSONObject().put("source",source).put("status",status).put("raw_status_code",statusCode).put("callback_count",callbacks).put("valid_sample_count",validSamples)
   .put("received_wall_ms",receivedWall).put("received_elapsed_ms",receivedElapsed).put("callback_age_ms",receivedElapsed<0?JSONObject.NULL:Math.max(0,elapsed-receivedElapsed))
   .put("car_value_elapsed_ms",carTimestamp).put("freshness",freshness(elapsed,wall)).put("physical_sensor_origin_verified",false);
  if(latest!=null){Location l=latest;JSONObject p=new JSONObject().put("latitude",l.getLatitude()).put("longitude",l.getLongitude()).put("provider",l.getProvider())
    .put("fix_wall_ms",l.getTime()).put("fix_elapsed_nanos",l.getElapsedRealtimeNanos()).put("mock",l.isFromMockProvider());
   p.put("accuracy_m",l.hasAccuracy()?finite(l.getAccuracy()):JSONObject.NULL).put("altitude_m",l.hasAltitude()?finite(l.getAltitude()):JSONObject.NULL)
    .put("speed_mps",l.hasSpeed()?finite(l.getSpeed()):JSONObject.NULL).put("bearing_deg",l.hasBearing()?finite(l.getBearing()):JSONObject.NULL);
   j.put("location",p);
  }else j.put("location",JSONObject.NULL);
  return j;
 }
}
