package com.theta.navihatch;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.libraries.navigation.NavigationView;
import java.util.concurrent.*;

public class MainActivity extends Activity {
 private NaviApp app;private LinearLayout root,side,results;private ScrollView sideScroll;private FrameLayout mapBox;private TextView status,mapHint;
 private TextView probeStatus;private Button sync,variant,open;private EditText query;private CheckBox useCenter;private Button search;
 private NavigationView mapView;private boolean started,resumed,viewStarted,viewResumed,refreshing,wide,settingsOpen;
 private final Runnable update=this::refresh;private final ExecutorService network=Executors.newSingleThreadExecutor();
 @Override public void onCreate(Bundle b){super.onCreate(b);app=(NaviApp)getApplication();getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(8),dp(14),dp(8));root.setBackgroundColor(0xff101a23);setContentView(root);
  root.setOnApplyWindowInsetsListener((v,in)->{root.setPadding(dp(14)+in.getSystemWindowInsetLeft(),dp(8)+in.getSystemWindowInsetTop(),dp(14)+in.getSystemWindowInsetRight(),dp(8)+in.getSystemWindowInsetBottom());return in;});
  root.addView(text("THETA / NAVI HATCH   0.2.1",22,0xff6de3c7));status=text("",12,0xffbfd1dc);root.addView(status);
  LinearLayout top=new LinearLayout(this);root.addView(top);addRow(top,button("地図を開始","start_sdk",this::startMaps),button("API設定","settings",this::settings),button("地図を広く","wide",()->{wide=!wide;sideScroll.setVisibility(wide?View.GONE:View.VISIBLE);}));
  LinearLayout body=new LinearLayout(this);root.addView(body,new LinearLayout.LayoutParams(-1,0,1));mapBox=new FrameLayout(this);body.addView(mapBox,new LinearLayout.LayoutParams(0,-1,1.5f));mapHint=text("Navigation SDKの実地図を表示します。\nAPI設定 → 地図を開始\n\n模擬地図への自動切替はしません。",18,Color.WHITE);mapBox.addView(mapHint);
  sideScroll=new ScrollView(this);side=new LinearLayout(this);side.setOrientation(LinearLayout.VERTICAL);side.setPadding(dp(12),0,0,0);sideScroll.addView(side);body.addView(sideScroll,new LinearLayout.LayoutParams(0,-1,1));
  open=button("車と同じ場所をタブレットで開く","open_tablet",()->cmd("open_tablet",1,0,""));side.addView(open);
  side.addView(button("タブレットの地図を閉じる","close_tablet",()->cmd("close_tablet",1,0,"")));
  sync=button("","link",()->cmd("link",app.cameras.linked?0:1,0,""));side.addView(sync);
  side.addView(button("タブレットの表示位置を車へ送る","send_car",()->cmd("send_car",1,0,"")));
  LinearLayout zoom=new LinearLayout(this);side.addView(zoom);addRow(zoom,button("＋","zoom_in",()->cmd("zoom",1.25,0,"")),button("−","zoom_out",()->cmd("zoom",.8,0,"")),button("現在地","recenter",()->cmd("home",1,0,"")));
  variant=button("","variant",()->cmd("variant",1,0,app.variant.equals("map_only")?"minimal":app.variant.equals("minimal")?"full":"map_only"));side.addView(variant);
  side.addView(button("渋滞表示 ON/OFF","traffic",()->cmd("traffic",app.traffic?0:1,0,"")));
  side.addView(text("GEMINI / 場所を探す",17,0xff6de3c7));query=new EditText(this);query.setTextColor(Color.WHITE);query.setHintTextColor(0xff9daeb9);query.setHint("例：この近くで子連れ向けのランチ");query.setMinLines(2);query.setContentDescription("theta:search_query");side.addView(query);
  useCenter=new CheckBox(this);useCenter.setText("表示中の地図中心を検索に使う");useCenter.setTextColor(Color.WHITE);useCenter.setChecked(true);side.addView(useCenter);
  side.addView(text("検索時に入力文と、選択した場合は地図中心をGoogleへ送信します。API利用料が発生する場合があります。",11,0xffaebbc5));
  search=button("Geminiで検索","search",this::searchPlaces);side.addView(search);results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);side.addView(results);
  side.addView(text("TEST LAB / 停車中に検証",15,0xffffc962));side.addView(button("テストモード ON/OFF","test_mode",()->cmd(app.testMode?"disable":"enable",1,0,"")));
  side.addView(button("車両位置情報の診断 ON/OFF","vehicle_probe",this::toggleVehicleProbe));probeStatus=text("",12,0xffbfd1dc);side.addView(probeStatus);
  side.addView(button("接続パネルの模擬 ON/OFF","simulate_connection",()->{if(app.testMode)cmd("connect",app.simulatedConnection?0:1,0,"");else message("先にテストモードをONにしてください");}));
  side.addView(button("ログに目印","mark",()->cmd("mark",1,0,"MANUAL_CHECK")));side.addView(button("SDK地図の画像を保存","snapshot",()->cmd("snapshot",1,0,"")));
  side.addView(button("ライセンス・表示について","licenses",this::licenses));
  app.listeners.add(update);refresh();app.event("tablet_lifecycle","created","");
 }
 private void toggleVehicleProbe(){if(app.vehicleProbe.enabled){cmd("vehicle_probe",0,0,"");return;}if(!app.testMode){message("先にテストモードをONにしてください。診断中は位置座標を端末内ログへ保存します。");return;}if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},22);return;}cmd("vehicle_probe",1,0,"");}
 private void startMaps(){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},21);return;}app.initialize(this);}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==22){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)cmd("vehicle_probe",1,0,"");else message("車両位置診断には正確な位置情報の許可が必要です。");}if(r==21){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)app.initialize(this);else message("Navigation SDKの初期化には位置情報の許可が必要です。設定で確認してください。");}}
 private void cmd(String c,double a,double b,String v){try{app.command("tablet_ui",c,a,b,v);}catch(Exception e){message(e.getMessage());}}
 private void refresh(){if(status==null||refreshing)return;refreshing=true;try{
  status.setText(app.sdkStatus+"\n車:"+(app.connection==2?"接続":"未接続")+"  SDK地図 車:"+app.maps.carLoaded+" / タブレット:"+app.maps.tabletLoaded+"  テスト:"+app.testMode+"\n"+app.lastEvent);
  if(probeStatus!=null)probeStatus.setText(app.vehicleProbe.summary());
  if(useCenter!=null){CameraState center=app.tabletOpen?app.cameras.tablet:app.cameras.car;useCenter.setText(String.format(java.util.Locale.US,"地図中心 (%.4f, %.4f) を検索に使う",center.latitude,center.longitude));}
  sync.setText(app.cameras.linked?"同期中：両方の地図が動く（切替）":"独立：タブレットだけ動く（切替）");variant.setText("車のボタン："+(app.variant.equals("minimal")?"最少：PAN・＋":app.variant.equals("full")?"PAN・＋・−・現在地":"PAN・＋・−"));
  if(!app.tabletOpen&&mapView!=null)destroyTablet();
  if(app.tabletOpen&&app.navigatorReady&&mapView==null)createTablet();
  mapHint.setVisibility(mapView==null?View.VISIBLE:View.GONE);if(!app.tabletOpen)mapHint.setText("タブレットの地図を閉じています。\n車載側のViewは破棄しません。\n\n右の『車と同じ場所をタブレットで開く』で再表示。");
 }finally{refreshing=false;}}
 private void createTablet(){try{
  NavigationView v=new NavigationView(this,new GoogleMapOptions().compassEnabled(false).zoomControlsEnabled(false).mapToolbarEnabled(false));mapView=v;
  v.onCreate(null);v.setNavigationUiEnabled(false);v.setHeaderEnabled(false);v.setEtaCardEnabled(false);v.setRecenterButtonEnabled(false);v.setSpeedometerEnabled(false);v.setSpeedLimitIconEnabled(false);v.setReportIncidentButtonEnabled(false);v.setTrafficIncidentCardsEnabled(false);v.setTrafficPromptsEnabled(false);v.setTripProgressBarEnabled(false);
  mapBox.addView(v,new FrameLayout.LayoutParams(-1,-1));if(started){v.onStart();viewStarted=true;}if(resumed){v.onResume();viewResumed=true;}app.tabletGeneration++;
  v.getMapAsync(map->{if(mapView==v&&!isDestroyed())app.maps.bind(false,map);});app.event("tablet_sdk","view_created",String.valueOf(app.tabletGeneration));
 }catch(Exception e){destroyTablet();message("地図Viewを開始できません: "+e.getClass().getSimpleName());app.event("tablet_sdk","create_failed",e.getClass().getSimpleName());}}
 private void destroyTablet(){NavigationView old=mapView;mapView=null;if(old==null)return;app.maps.detach(false);if(viewResumed)old.onPause();if(viewStarted)old.onStop();old.onDestroy();viewResumed=false;viewStarted=false;mapBox.removeView(old);app.event("tablet_sdk","view_destroyed","");}
 @Override protected void onStart(){super.onStart();started=true;if(mapView!=null&&!viewStarted){mapView.onStart();viewStarted=true;}}
 @Override protected void onResume(){super.onResume();resumed=true;if(mapView!=null&&!viewResumed){mapView.onResume();viewResumed=true;}refresh();}
 @Override protected void onPause(){if(mapView!=null&&viewResumed){mapView.onPause();viewResumed=false;}resumed=false;super.onPause();}
 @Override protected void onStop(){if(mapView!=null&&viewStarted){mapView.onStop();viewStarted=false;}started=false;super.onStop();}
 @Override protected void onDestroy(){app.listeners.remove(update);destroyTablet();network.shutdownNow();super.onDestroy();}
 @Override public void onConfigurationChanged(Configuration c){super.onConfigurationChanged(c);if(mapView!=null)mapView.onConfigurationChanged(c);}
 @Override public void onTrimMemory(int level){super.onTrimMemory(level);if(mapView!=null)mapView.onTrimMemory(level);}
 @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);if(mapView!=null){Bundle sdk=new Bundle();mapView.onSaveInstanceState(sdk);b.putBundle("tablet_sdk",sdk);}}
 private void settings(){LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(dp(20),dp(8),dp(20),dp(8));
  EditText nav=keyField(form,"Navigation SDKキー",!app.secrets.read("navigation").isEmpty());EditText gem=keyField(form,"Gemini APIキー",!app.secrets.read("gemini").isEmpty());EditText places=keyField(form,"Places APIキー（任意・空欄なら地図キー）",!app.secrets.read("places").isEmpty());
  EditText model=new EditText(this);model.setSingleLine(true);model.setText(getSharedPreferences("settings",0).getString("geminiModel","gemini-2.5-flash"));form.addView(text("Geminiモデル",13,Color.WHITE));form.addView(model);
  form.addView(text("空欄は保存済みキーを維持します。地図キー変更はプロセス再起動後に反映。キーは端末内で暗号化し、診断ログには含めません。",12,0xffbaccd5));
  AlertDialog d=new AlertDialog.Builder(this).setTitle("API設定").setView(form).setPositiveButton("保存",(x,w)->{try{if(nav.length()>0)app.secrets.write("navigation",nav.getText().toString().trim());if(gem.length()>0)app.secrets.write("gemini",gem.getText().toString().trim());if(places.length()>0)app.secrets.write("places",places.getText().toString().trim());String name=model.getText().toString().trim();if(!name.matches("[A-Za-z0-9._-]+"))throw new IllegalArgumentException("モデル名が不正です");getSharedPreferences("settings",0).edit().putString("geminiModel",name).apply();app.event("settings","saved","keys omitted");message("保存しました。地図を開始してください。");}catch(Exception e){message("設定保存失敗: "+e.getClass().getSimpleName());}}).setNeutralButton("キーを全消去",(x,w)->{try{for(String k:new String[]{"navigation","gemini","places"})app.secrets.write(k,"");message("削除しました。既に起動したSDKのキーを消すにはアプリのプロセスを再起動してください。");}catch(Exception e){message("削除に失敗しました");}}).setNegativeButton("戻る",null).create();settingsOpen=true;d.setOnDismissListener(dialog->settingsOpen=false);d.show();d.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
 }
 private EditText keyField(LinearLayout form,String label,boolean configured){form.addView(text(label+(configured?"（設定済み）":""),13,Color.WHITE));EditText e=new EditText(this);e.setSingleLine(true);e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);e.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);form.addView(e);return e;}
 private void searchPlaces(){String q=query.getText().toString().trim(),key=app.secrets.read("gemini");if(q.isEmpty()){message("検索内容を入力してください");return;}if(key.isEmpty()){message("API設定でGeminiキーを入力してください");return;}
  String model=getSharedPreferences("settings",0).getString("geminiModel","gemini-2.5-flash");CameraState center=app.tabletOpen?app.cameras.tablet:app.cameras.car;boolean location=useCenter.isChecked();search.setEnabled(false);results.removeAllViews();results.addView(text("Google Mapsを参照して検索中…",14,Color.WHITE));app.event("gemini","request","query and coordinates omitted");
  network.execute(()->{try{GeminiSearch.Result answer=GeminiSearch.search(key,model,q,center,location);runOnUiThread(()->{if(isDestroyed())return;search.setEnabled(true);showResults(answer);app.event("gemini","completed","sources="+answer.places.size());});}catch(Exception e){runOnUiThread(()->{if(isDestroyed())return;search.setEnabled(true);results.removeAllViews();results.addView(text("検索失敗: "+e.getMessage(),14,0xffffc962));app.event("gemini","failed",e.getClass().getSimpleName());});}});
 }
 private void showResults(GeminiSearch.Result answer){results.removeAllViews();TextView body=text(answer.text,15,Color.WHITE);body.setTextIsSelectable(true);results.addView(body);results.addView(text("Google Maps 出典・候補",14,0xff6de3c7));for(GeminiSearch.Place p:answer.places){results.addView(text(p.title,16,Color.WHITE));results.addView(button("Google Mapsの出典を開く","source",()->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(p.uri)));}catch(ActivityNotFoundException e){message("リンクを開くアプリがありません");}}));Button locate=button("このアプリの地図に表示","locate",()->locate(p));locate.setEnabled(!p.placeId.isEmpty());results.addView(locate);}}
 private void locate(GeminiSearch.Place p){String key=app.secrets.read("places");if(key.isEmpty())key=app.secrets.read("navigation");if(key.isEmpty()){message("Places API（New）を有効にしたキーが必要です。API設定を確認してください。");return;}final String apiKey=key;message("Place IDから位置を取得中…");network.execute(()->{try{double[] point=GeminiSearch.locate(apiKey,p.placeId,certificateSha1());runOnUiThread(()->{if(!isDestroyed())app.maps.select(point[0],point[1],p.title);});}catch(Exception e){runOnUiThread(()->{if(!isDestroyed())message("場所の表示に失敗: "+e.getMessage()+"\nPlaces API（New）を有効にしてください。出典リンクはそのまま利用できます。");});}});}
 @SuppressWarnings("deprecation") private String certificateSha1()throws Exception{byte[] cert=getPackageManager().getPackageInfo(getPackageName(),PackageManager.GET_SIGNATURES).signatures[0].toByteArray();byte[] hash=java.security.MessageDigest.getInstance("SHA-1").digest(cert);StringBuilder out=new StringBuilder();for(byte b:hash)out.append(String.format(java.util.Locale.US,"%02X",b));return out.toString();}
 private void licenses(){StringBuilder s=new StringBuilder("Google Navigation SDK 7.9.0\nGoogleのロゴ・著作権表示は維持します。Android AutoのシステムUIはアプリから消せません。\nこの版は地図表示・操作と検索の試験用です。音声案内・走行ナビは開始しません。\n\n");try{for(String name:getAssets().list("legal")){s.append(name).append('\n');try(java.io.InputStream in=getAssets().open("legal/"+name)){java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1)bytes.write(buffer,0,count);s.append(new String(bytes.toByteArray(),java.nio.charset.StandardCharsets.UTF_8)).append('\n');}}}catch(Exception ignored){}TextView t=text(s.toString(),12,Color.WHITE);ScrollView scroll=new ScrollView(this);scroll.addView(t);new AlertDialog.Builder(this).setTitle("ライセンス").setView(scroll).setPositiveButton("閉じる",null).show();}
 private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(3),dp(4),dp(3),dp(4));return t;}
 private Button button(String s,String id,Runnable r){Button b=new Button(this);b.setText(s);b.setTextSize(13);b.setAllCaps(false);b.setContentDescription("theta:"+id);b.setOnClickListener(v->r.run());return b;}
 private void addRow(LinearLayout row,Button...buttons){for(Button b:buttons)row.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));}
 private void message(String s){new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show();}
 @Override public boolean dispatchKeyEvent(KeyEvent e){if(app!=null&&app.testMode&&!settingsOpen)app.event("tablet_key","key",e.getKeyCode()+":"+e.getAction());return super.dispatchKeyEvent(e);}
}
