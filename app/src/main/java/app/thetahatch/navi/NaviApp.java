package app.thetahatch.navi;
import android.app.*;
import android.os.*;
import android.util.Log;
import androidx.car.app.connection.CarConnection;
import com.google.android.libraries.navigation.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NaviApp extends Application {
 public final Handler main=new Handler(Looper.getMainLooper());
 public final List<Runnable> listeners=new ArrayList<>();
 public final CameraLink cameras=new CameraLink();
 public RealMaps maps;public SecretStore secrets;public VehicleLocationProbe vehicleProbe;
 public boolean testMode,simulatedConnection,carBound,carPan,tabletOpen=true,traffic;
 public boolean sdkConfigured,navigatorReady,initializing;public String sdkStatus="APIキーを設定してください",variant="map_only",host="",lastEvent="起動";
 public int connection,surfaceWidth,surfaceHeight,carScroll,carScale,carClick,carFling,carGeneration,tabletGeneration;
 public String visibleArea="",stableArea="";public long seq;public Navigator navigator;
 public Runnable captureCar;private CarConnection carConnection;
 private String configuredKey="";
 @Override public void onCreate(){super.onCreate();secrets=new SecretStore(this);maps=new RealMaps(this);vehicleProbe=new VehicleLocationProbe(this);testMode=getSharedPreferences("settings",0).getBoolean("testMode",false);
  carConnection=new CarConnection(this);carConnection.getType().observeForever(v->{connection=v==null?0:v;event("connection","actual",String.valueOf(connection));});event("lifecycle","process_started",BuildConfig.VERSION_NAME);
 }
 public boolean carLayout(){return connection==CarConnection.CONNECTION_TYPE_PROJECTION||simulatedConnection;}
 public boolean configureSdk(){
  String key=secrets.read("navigation");if(key.isEmpty()){sdkStatus="Navigation SDKキーが未設定です";return false;}
  if(sdkConfigured){if(!key.equals(configuredKey)){sdkStatus="地図キーを変更しました。アプリのプロセス再起動が必要です";return false;}return true;}
  try{NavigationApi.setApiKey(key);configuredKey=key;sdkConfigured=true;return true;}catch(Exception e){sdkStatus="SDKキー設定エラー: "+e.getClass().getSimpleName();return false;}
 }
 public void initialize(Activity activity){
  if(!configureSdk()){event("sdk","key_required","");return;}if(navigatorReady||initializing)return;
  initializing=true;sdkStatus="Navigation SDKを初期化中";event("sdk","initializing","");
  NavigationApi.NavigatorListener listener=new NavigationApi.NavigatorListener(){public void onNavigatorReady(Navigator n){navigator=n;navigatorReady=true;initializing=false;sdkStatus="Navigation SDK準備完了";event("sdk","ready","");}public void onError(int code){initializing=false;sdkStatus="SDK初期化エラー code="+code+"（キー・課金・権限・利用規約を確認）";event("sdk","error",String.valueOf(code));}};
  try{if(activity!=null)NavigationApi.getNavigator(activity,listener);else {if(!NavigationApi.areTermsAccepted(this)){initializing=false;sdkStatus="タブレットで地図を開始し、利用規約を確認してください";event("sdk","terms_required","");return;}NavigationApi.getNavigator(this,listener);}}
  catch(Exception e){initializing=false;sdkStatus="SDK初期化失敗: "+e.getClass().getSimpleName();event("sdk","exception",e.getClass().getSimpleName());}
 }
 public JSONObject state(){JSONObject j=new JSONObject();try{
  j.put("schema",3).put("version",BuildConfig.VERSION_NAME).put("test_mode",testMode).put("actual_connection",connection).put("simulated_connection",simulatedConnection).put("car_layout",carLayout())
   .put("vehicle_location_probe",vehicleProbe==null?JSONObject.NULL:vehicleProbe.state()).put("car_bound",carBound).put("car_pan_mode",carPan).put("host",host).put("surface_width",surfaceWidth).put("surface_height",surfaceHeight).put("visible_area",visibleArea).put("stable_area",stableArea)
   .put("map_source","google_navigation_sdk").put("sdk_version","7.9.0").put("sdk_configured",sdkConfigured).put("navigator_ready",navigatorReady).put("sdk_status",sdkStatus).put("guidance_active",false)
   .put("tablet_open",tabletOpen).put("linked",cameras.linked).put("traffic",traffic).put("variant",variant)
   .put("car_map_ready",maps!=null&&maps.car!=null).put("tablet_map_ready",maps!=null&&maps.tablet!=null).put("car_map_loaded",maps!=null&&maps.carLoaded).put("tablet_map_loaded",maps!=null&&maps.tabletLoaded)
   .put("car_map_generation",carGeneration).put("tablet_map_generation",tabletGeneration).put("car_camera",cameraJson(cameras.car)).put("tablet_camera",cameraJson(cameras.tablet))
   .put("car_scroll_count",carScroll).put("car_scale_count",carScale).put("car_click_count",carClick).put("car_fling_count",carFling).put("seq",seq).put("last_event",lastEvent);
 }catch(JSONException e){throw new IllegalStateException(e);}return j;}
 private JSONObject cameraJson(CameraState c)throws JSONException{return new JSONObject().put("latitude",c.latitude).put("longitude",c.longitude).put("zoom",c.zoom).put("bearing",c.bearing).put("tilt",c.tilt);}
 public void event(String source,String type,String detail){seq++;lastEvent=source+" / "+type+" / "+detail;
  try{JSONObject e=new JSONObject().put("seq",seq).put("time_ms",System.currentTimeMillis()).put("source",source).put("event",type).put("detail",detail);
   File f=new File(getFilesDir(),"events.jsonl");if(f.length()>1048576){File old=new File(getFilesDir(),"events.previous.jsonl");old.delete();f.renameTo(old);}
   try(FileOutputStream o=new FileOutputStream(f,true)){o.write((e+"\n").getBytes(StandardCharsets.UTF_8));}Log.i("ThetaNavi",e.toString());saveState();
  }catch(Exception e){Log.e("ThetaNavi","Diagnostic write failed");}
  for(Runnable r:new ArrayList<>(listeners))r.run();
 }
 public void saveState(){try{File f=new File(getFilesDir(),"state.tmp");try(FileOutputStream o=new FileOutputStream(f)){o.write(state().toString(2).getBytes(StandardCharsets.UTF_8));}if(!f.renameTo(new File(getFilesDir(),"state.json")))Log.e("ThetaNavi","State rename failed");}catch(Exception e){Log.e("ThetaNavi","State write failed");}}
 public void command(String source,String cmd,double a,double b,String value){
  if(!Double.isFinite(a)||!Double.isFinite(b))throw new IllegalArgumentException("finite arguments required");
  switch(cmd){
   case "enable":testMode=true;getSharedPreferences("settings",0).edit().putBoolean("testMode",true).apply();break;
   case "disable":vehicleProbe.setEnabled(false);testMode=false;simulatedConnection=false;getSharedPreferences("settings",0).edit().putBoolean("testMode",false).apply();break;
   case "vehicle_probe":vehicleProbe.setEnabled(a!=0);break;
   case "connect":simulatedConnection=a!=0;break;
   case "variant":if(!Arrays.asList("full","map_only","minimal").contains(value))throw new IllegalArgumentException("variant full|map_only|minimal");variant=value;break;
   case "link":cameras.setLinked(a!=0);break;
   case "open_tablet":cameras.openCarOnTablet();tabletOpen=true;break;
   case "close_tablet":tabletOpen=false;break;
   case "send_car":cameras.sendTabletToCar();break;
   case "zoom":maps.zoom(a);break;
   case "pan":maps.pan(a,b);break;
   case "home":maps.recenter();break;
   case "north":{CameraState c=cameras.car;maps.moveBoth(new CameraState(c.latitude,c.longitude,c.zoom,0,0));break;}
   case "reset":maps.moveBoth(CameraState.osaka());break;
   case "overview":{CameraState c=cameras.car;maps.moveBoth(new CameraState(c.latitude,c.longitude,12,0,0));break;}
   case "goto":maps.moveBoth(new CameraState(a,b,16,0,0));break;
   case "traffic":traffic=a!=0;maps.applyTraffic();break;
   case "snapshot":maps.snapshots();break;
   case "mark":case "status":break;
   default:throw new IllegalArgumentException("Unknown command: "+cmd);
  }event(source,cmd,cmd.equals("mark")?value:String.format(Locale.US,"a=%.3f b=%.3f %s",a,b,value));
 }
}
