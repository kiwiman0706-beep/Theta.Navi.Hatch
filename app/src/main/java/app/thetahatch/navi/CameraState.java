package app.thetahatch.navi;

/** A camera pose, independent of SDK/View lifecycles. */
public final class CameraState {
 public final double latitude, longitude; public final float zoom, bearing, tilt;
 public CameraState(double lat,double lng,float z,float b,float t){
  if(!Double.isFinite(lat)||!Double.isFinite(lng)||!Float.isFinite(z)||!Float.isFinite(b)||!Float.isFinite(t)||Math.abs(lat)>85||Math.abs(lng)>180)throw new IllegalArgumentException("Invalid camera");
  latitude=lat;longitude=lng;zoom=Math.max(2,Math.min(21,z));bearing=((b%360)+360)%360;tilt=Math.max(0,Math.min(67.5f,t));
 }
 public static CameraState osaka(){return new CameraState(34.6652,135.5018,15,0,0);}
 public boolean near(CameraState o){return o!=null&&Math.abs(latitude-o.latitude)<1e-7&&Math.abs(longitude-o.longitude)<1e-7&&Math.abs(zoom-o.zoom)<.001&&Math.min(Math.abs(bearing-o.bearing),360-Math.abs(bearing-o.bearing))<.01&&Math.abs(tilt-o.tilt)<.01;}
}
