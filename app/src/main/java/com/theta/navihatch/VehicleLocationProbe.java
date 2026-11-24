package com.theta.navihatch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.*;
import androidx.car.app.CarContext;
import androidx.car.app.hardware.CarHardwareManager;
import androidx.car.app.hardware.common.*;
import androidx.car.app.hardware.info.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Explicit test-mode subscription. Does not change any map, Navigator, or system location. */
public final class VehicleLocationProbe {
 private final NaviApp app;
 public final LocationProbeState car=new LocationProbeState("android_auto_car_hardware");
 public final LocationProbeState device=new LocationProbeState("android_location_manager_gps");
 public boolean enabled;
 private CarContext host;private CarSensors sensors;private OnCarDataAvailableListener<CarHardwareLocation> carListener;
 private LocationManager locations;private LocationListener deviceListener;private int generation,carApi;
 private long lastWrite=-1;private String stopReason="NOT_STARTED",sessionId="";
 private Runnable timeout;
 public VehicleLocationProbe(NaviApp app){this.app=app;}
 public void attach(CarContext context){if(host==context)return;stopSubscriptions("HOST_REPLACED");host=context;carApi=context.getCarAppApiLevel();if(enabled)start();}
 public void detach(CarContext context){if(host!=context)return;stopSubscriptions("DISCONNECTED");host=null;carApi=0;car.status="DISCONNECTED";device.status="STOPPED";publish(true);}
 public void setEnabled(boolean value){
  if(value&&!app.testMode)throw new IllegalStateException("先にテストモードをONにしてください");
  enabled=value;if(value)start();else{stopSubscriptions("USER_STOPPED");car.status="STOPPED";device.status="STOPPED";publish(true);}
 }
 private void start(){
  stopSubscriptions("RESTART");car.reset("WAITING_FOR_HOST");device.reset("NOT_STARTED");sessionId=Long.toString(System.currentTimeMillis());lastWrite=-1;
  if(host==null){publish(true);return;}
  if(app.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){car.status="PERMISSION_REQUIRED";device.status="PERMISSION_REQUIRED";publish(true);return;}
  int epoch=generation;long startedElapsed=SystemClock.elapsedRealtime();
  if(carApi<3){car.status="HOST_API_BELOW_3";}else try{
   sensors=host.getCarService(CarHardwareManager.class).getCarSensors();car.status="WAITING_FOR_CALLBACK";
   carListener=data->{if(!enabled||epoch!=generation)return;CarValue<Location> v=data.getLocation();car.accept(v.getStatus(),v.getTimestampMillis(),v.getValue(),SystemClock.elapsedRealtime(),System.currentTimeMillis());publish(false);};
   sensors.addCarHardwareLocationListener(CarSensors.UPDATE_RATE_NORMAL,command->app.main.post(command),carListener);
  }catch(SecurityException e){car.status="PERMISSION_DENIED";}catch(RuntimeException e){car.status="SUBSCRIBE_ERROR_"+e.getClass().getSimpleName();}
  try{
   locations=(LocationManager)app.getSystemService(Context.LOCATION_SERVICE);
   if(locations==null||!locations.getAllProviders().contains(LocationManager.GPS_PROVIDER)){device.status="GPS_PROVIDER_ABSENT";}
   else {
    device.status=locations.isProviderEnabled(LocationManager.GPS_PROVIDER)?"WAITING_FOR_CALLBACK":"GPS_DISABLED";
    deviceListener=new LocationListener(){
     @Override public void onLocationChanged(Location l){if(!enabled||epoch!=generation)return;device.accept(CarValue.STATUS_SUCCESS,-1,l,SystemClock.elapsedRealtime(),System.currentTimeMillis());publish(false);}
     @Override public void onProviderDisabled(String p){if(epoch==generation){device.latest=null;device.status="GPS_DISABLED";publish(true);}}
     @Override public void onProviderEnabled(String p){if(epoch==generation){device.latest=null;device.status="WAITING_FOR_CALLBACK";publish(true);}}
     @Override public void onStatusChanged(String p,int s,Bundle extras){}
    };
    locations.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,deviceListener,Looper.getMainLooper());
   }
  }catch(SecurityException e){device.status="PERMISSION_DENIED";}catch(RuntimeException e){device.status="SUBSCRIBE_ERROR_"+e.getClass().getSimpleName();}
  timeout=()->{if(!enabled||epoch!=generation)return;if(SystemClock.elapsedRealtime()-startedElapsed>=30000){if(car.callbacks==0&&car.status.equals("WAITING_FOR_CALLBACK"))car.status="NO_CALLBACK_AFTER_30S";if(device.callbacks==0&&device.status.equals("WAITING_FOR_CALLBACK"))device.status="NO_CALLBACK_AFTER_30S";}publish(true);if(enabled&&epoch==generation)app.main.postDelayed(timeout,5000);};app.main.postDelayed(timeout,5000);publish(true);
 }
 private void stopSubscriptions(String reason){
  generation++;stopReason=reason;if(timeout!=null)app.main.removeCallbacks(timeout);timeout=null;
  if(sensors!=null&&carListener!=null)try{sensors.removeCarHardwareLocationListener(carListener);}catch(RuntimeException ignored){}
  if(locations!=null&&deviceListener!=null)try{locations.removeUpdates(deviceListener);}catch(RuntimeException ignored){}
  sensors=null;carListener=null;locations=null;deviceListener=null;
 }
 public JSONObject state(){long elapsed=SystemClock.elapsedRealtime(),wall=System.currentTimeMillis();JSONObject j=new JSONObject();try{
  j.put("enabled",enabled).put("session_id",sessionId).put("host_attached",host!=null).put("car_api_level",carApi).put("last_stop_reason",stopReason)
   .put("fine_location_granted",app.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
   .put("navigation_location_injected",false).put("carplay_probe","NOT_IMPLEMENTED_ANDROID_APK")
   .put("car",car.json(elapsed,wall)).put("device_gps",device.json(elapsed,wall));
  boolean paired=enabled&&car.status.equals("SUCCESS")&&device.status.equals("SUCCESS")&&car.freshness(elapsed,wall).equals("RECENT")&&device.freshness(elapsed,wall).equals("RECENT")
    &&Math.abs(car.latest.getTime()-device.latest.getTime())<=5000&&!car.latest.isFromMockProvider()&&!device.latest.isFromMockProvider();
  j.put("comparison_eligible",paired).put("separation_m",paired?car.latest.distanceTo(device.latest):JSONObject.NULL)
   .put("comparison_note","API経路の比較。距離差は精度や物理センサーの由来を証明しません。");
 }catch(JSONException e){throw new IllegalStateException(e);}return j;}
 public String summary(){return "車両位置診断: "+(enabled?"ON":"OFF")+" / Car API "+carApi+"\n車API: "+car.status+" ("+car.validSamples+") / "+car.freshness(SystemClock.elapsedRealtime(),System.currentTimeMillis())+"\n端末GPS API: "+device.status+" ("+device.validSamples+")"+"\n"+"物理的なGPS由来・車速補正は未判定。ナビ測位には注入しません。";}
 private void publish(boolean force){long now=SystemClock.elapsedRealtime();if(!force&&lastWrite>=0&&now-lastWrite<1000){app.saveState();return;}lastWrite=now;
  try{JSONObject j=state().put("record_wall_ms",System.currentTimeMillis());File f=new File(app.getFilesDir(),"vehicle-location.jsonl");if(f.length()>1048576){File old=new File(app.getFilesDir(),"vehicle-location.previous.jsonl");old.delete();f.renameTo(old);}try(FileOutputStream out=new FileOutputStream(f,true)){out.write((j+"\n").getBytes(StandardCharsets.UTF_8));}}catch(Exception e){android.util.Log.e("ThetaNavi","Vehicle probe log write failed");}
  app.event("vehicle_probe","status",car.status+" / "+device.status);
 }
}
