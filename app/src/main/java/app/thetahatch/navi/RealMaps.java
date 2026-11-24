package app.thetahatch.navi;
import android.graphics.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.io.*;

/** Car and tablet maps have independent owners. Only camera poses are shared. */
public final class RealMaps {
 public GoogleMap car,tablet;public boolean carLoaded,tabletLoaded;
 private final NaviApp app;private long lastLog;private Marker carPin,tabletPin;private LatLng pin;private String pinTitle="";
 public RealMaps(NaviApp a){app=a;}
 private CameraState read(GoogleMap m){CameraPosition c=m.getCameraPosition();return new CameraState(c.target.latitude,c.target.longitude,c.zoom,c.bearing,c.tilt);}
 public void bind(boolean isCar,GoogleMap map){
  if(isCar){car=map;carLoaded=false;}else{tablet=map;tabletLoaded=false;}
  UiSettings ui=map.getUiSettings();ui.setCompassEnabled(false);ui.setZoomControlsEnabled(false);ui.setMyLocationButtonEnabled(false);ui.setMapToolbarEnabled(false);ui.setIndoorLevelPickerEnabled(false);ui.setAllGesturesEnabled(!isCar);
  map.setTrafficEnabled(app.traffic);
  if(app.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED)map.setMyLocationEnabled(true);
  app.cameras.attach(isCar,c->{if((isCar?car:tablet)!=map)return;map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(c.latitude,c.longitude),c.zoom,c.tilt,c.bearing)));});
  map.setOnCameraMoveListener(()->{if((isCar?car:tablet)!=map)return;app.cameras.changed(isCar,read(map));long now=android.os.SystemClock.uptimeMillis();if(now-lastLog>250){lastLog=now;app.event(isCar?"car_camera":"tablet_camera","move","");}});
  map.setOnCameraIdleListener(()->{if((isCar?car:tablet)!=map)return;app.cameras.changed(isCar,read(map));app.event(isCar?"car_camera":"tablet_camera","idle","");});
  map.setOnMapLoadedCallback(()->{if((isCar?car:tablet)!=map)return;if(isCar)carLoaded=true;else tabletLoaded=true;app.event(isCar?"car_sdk":"tablet_sdk","map_loaded","");});
  refreshPin(isCar);app.event(isCar?"car_sdk":"tablet_sdk","map_ready","");
 }
 public void detach(boolean isCar){GoogleMap m=isCar?car:tablet;if(m!=null){if(isCar)app.cameras.car=read(m);else app.cameras.tablet=read(m);m.setOnCameraMoveListener(null);m.setOnCameraIdleListener(null);m.setOnMapLoadedCallback(null);}app.cameras.detach(isCar);if(isCar){car=null;carLoaded=false;carPin=null;}else{tablet=null;tabletLoaded=false;tabletPin=null;}app.saveState();}
 private GoogleMap control(){GoogleMap m=car!=null?car:tablet;if(m==null)throw new IllegalStateException("Real map is not ready");return m;}
 public void zoom(double factor){if(factor<=0)throw new IllegalArgumentException("positive scale required");control().moveCamera(CameraUpdateFactory.zoomBy((float)(Math.log(factor)/Math.log(2))));}
 public void pan(double x,double y){control().moveCamera(CameraUpdateFactory.scrollBy((float)x,(float)y));}
 public void recenter(){android.location.Location l=control().getMyLocation();if(l==null)throw new IllegalStateException("現在地をまだ取得できません。位置情報権限・GPSを確認してください");moveBoth(new CameraState(l.getLatitude(),l.getLongitude(),16,0,0));}
 public void moveBoth(CameraState c){app.cameras.set(true,c);app.cameras.set(false,c);}
 public void applyTraffic(){if(car!=null)car.setTrafficEnabled(app.traffic);if(tablet!=null)tablet.setTrafficEnabled(app.traffic);}
 public void select(double lat,double lng,String title){pin=new LatLng(lat,lng);pinTitle=title;refreshPin(true);refreshPin(false);moveBoth(new CameraState(lat,lng,17,0,0));app.tabletOpen=true;app.event("search","place_selected","");}
 private void refreshPin(boolean isCar){GoogleMap m=isCar?car:tablet;if(m==null||pin==null)return;Marker old=isCar?carPin:tabletPin;if(old!=null)old.remove();Marker marker=m.addMarker(new MarkerOptions().position(pin).title(pinTitle));if(isCar)carPin=marker;else tabletPin=marker;}
 public void snapshots(){snapshot(car,"car-map-only-NOT-host-UI.png");snapshot(tablet,"tablet-map-only.png");}
 private void snapshot(GoogleMap m,String name){File f=new File(app.getFilesDir(),name);f.delete();if(m==null)return;try{m.snapshot(b->{if(b==null)return;try(FileOutputStream o=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,o);app.event("sdk_snapshot","saved",name);}catch(Exception e){app.event("sdk_snapshot","failed",name);}});}catch(Exception e){app.event("sdk_snapshot","failed",name);}}
}
