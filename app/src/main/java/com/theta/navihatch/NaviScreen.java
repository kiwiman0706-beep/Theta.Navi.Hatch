package com.theta.navihatch;
import android.app.Presentation;
import android.content.Context;
import android.graphics.*;
import android.hardware.display.*;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.car.app.*;
import androidx.car.app.model.*;
import androidx.car.app.navigation.model.NavigationTemplate;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.*;
import com.google.android.gms.maps.*;
import com.google.android.libraries.navigation.NavigationViewForAuto;

public class NaviScreen extends Screen implements SurfaceCallback {
 private final NaviApp app;private Surface surface;private int dpi;
 private Rect visible=new Rect();private VirtualDisplay display;private Presentation presentation;private NavigationViewForAuto view;
 private boolean active=true,mounting;private String lastVariant="";private final Runnable refresh=this::refresh;
 public NaviScreen(CarContext c,NaviApp a){super(c);app=a;app.carBound=true;app.vehicleProbe.attach(c);app.listeners.add(refresh);c.getCarService(AppManager.class).setSurfaceCallback(this);
  getLifecycle().addObserver(new DefaultLifecycleObserver(){@Override public void onDestroy(@NonNull LifecycleOwner owner){active=false;app.vehicleProbe.detach(c);app.listeners.remove(refresh);destroyMap();surface=null;app.surfaceWidth=0;app.surfaceHeight=0;app.carBound=false;app.carPan=false;c.getCarService(AppManager.class).setSurfaceCallback(null);app.event("car_lifecycle","screen_destroyed","");}});
  app.event("car_lifecycle","screen_created","api="+c.getCarAppApiLevel());app.initialize(null);
 }
 private Action icon(int drawable,String cmd,double arg){return new Action.Builder().setIcon(new CarIcon.Builder(IconCompat.createWithResource(getCarContext(),drawable)).build()).setOnClickListener(()->{try{app.command("car_button",cmd,arg,0,"");}catch(Exception e){app.event("car_button","error",e.getClass().getSimpleName());}}).build();}
 @NonNull @Override public Template onGetTemplate(){
  ActionStrip.Builder actions=new ActionStrip.Builder().addAction(Action.PAN);
  if(!app.variant.equals("minimal"))actions.addAction(icon(R.drawable.ic_minus,"zoom",.8));
  if(app.variant.equals("full"))actions.addAction(icon(R.drawable.ic_home,"home",1));lastVariant=app.variant;
  return new NavigationTemplate.Builder().setActionStrip(new ActionStrip.Builder().addAction(icon(R.drawable.ic_plus,"zoom",1.25)).build()).setMapActionStrip(actions.build()).setPanModeListener(p->{app.carPan=p;app.event("car_host","pan_mode",String.valueOf(p));}).build();
 }
 private void refresh(){if(!active)return;if(!lastVariant.equals(app.variant))invalidate();if(view==null&&!mounting&&surface!=null&&surface.isValid()){if(app.navigatorReady)createMap();else placeholder();}}
 @Override public void onSurfaceAvailable(@NonNull SurfaceContainer sc){destroyMap();surface=sc.getSurface();dpi=sc.getDpi();app.surfaceWidth=sc.getWidth();app.surfaceHeight=sc.getHeight();app.event("car_surface","available",sc.getWidth()+"x"+sc.getHeight());}
 private void createMap(){if(dpi<=0||app.surfaceWidth<=0||app.surfaceHeight<=0)return;mounting=true;
  try{DisplayManager dm=(DisplayManager)getCarContext().getSystemService(Context.DISPLAY_SERVICE);display=dm.createVirtualDisplay("ThetaNaviCar",app.surfaceWidth,app.surfaceHeight,dpi,surface,DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY);
   if(display==null)throw new IllegalStateException("VirtualDisplay unavailable");presentation=new Presentation(getCarContext(),display.getDisplay());
   view=new NavigationViewForAuto(presentation.getContext(),new GoogleMapOptions().compassEnabled(false).zoomControlsEnabled(false).mapToolbarEnabled(false));
   NavigationViewForAuto owner=view;view.onCreate(null);view.onStart();view.onResume();presentation.setContentView(view);presentation.show();app.carGeneration++;
   view.getMapAsync(map->{if(!active||view!=owner)return;app.maps.bind(true,map);applyPadding();});app.event("car_sdk","view_created",String.valueOf(app.carGeneration));
  }catch(Exception e){destroyMap();app.event("car_sdk","create_failed",e.getClass().getSimpleName());placeholder();}finally{mounting=false;}
 }
 private void destroyMap(){app.maps.detach(true);NavigationViewForAuto old=view;view=null;if(old!=null){old.onPause();old.onStop();old.onDestroy();}if(presentation!=null){presentation.dismiss();presentation=null;}if(display!=null){display.release();display=null;}}
 @Override public void onSurfaceDestroyed(@NonNull SurfaceContainer sc){if(surface!=null&&sc.getSurface()!=null&&!surface.equals(sc.getSurface()))return;destroyMap();surface=null;app.surfaceWidth=0;app.surfaceHeight=0;visible.setEmpty();app.visibleArea="";app.stableArea="";app.event("car_surface","destroyed","");}
 @Override public void onVisibleAreaChanged(@NonNull Rect r){visible=new Rect(r);app.visibleArea=r.toShortString();applyPadding();app.event("car_surface","visible_area",app.visibleArea);}
 @Override public void onStableAreaChanged(@NonNull Rect r){app.stableArea=r.toShortString();app.event("car_surface","stable_area",app.stableArea);}
 private void applyPadding(){if(app.maps.car!=null&&!visible.isEmpty())app.maps.car.setPadding(Math.max(0,visible.left),Math.max(0,visible.top),Math.max(0,app.surfaceWidth-visible.right),Math.max(0,app.surfaceHeight-visible.bottom));}
 @Override public void onScroll(float x,float y){app.carScroll++;if(app.maps.car!=null)app.maps.car.moveCamera(CameraUpdateFactory.scrollBy(x,y));app.event("car_surface","scroll",x+","+y);}
 @Override public void onScale(float x,float y,float f){app.carScale++;if(app.maps.car!=null&&Float.isFinite(f)&&f>0){float delta=(float)(Math.log(f)/Math.log(2));if(x<0||y<0)app.maps.car.moveCamera(CameraUpdateFactory.zoomBy(delta));else app.maps.car.moveCamera(CameraUpdateFactory.zoomBy(delta,new Point((int)x,(int)y)));}app.event("car_surface","scale",Float.toString(f));}
 @Override public void onClick(float x,float y){app.carClick++;app.event("car_surface","click",x+","+y);}
 @Override public void onFling(float x,float y){app.carFling++;app.event("car_surface","fling",x+","+y+" (log only)");}
 private void placeholder(){if(surface==null||!surface.isValid())return;Canvas c=null;try{c=surface.lockCanvas(null);c.drawColor(Color.rgb(15,25,34));Paint p=new Paint(3);p.setColor(Color.WHITE);p.setTextSize(24);c.drawText("Theta.Navi.Hatch",28,65,p);p.setTextSize(20);c.drawText("タブレットでAPIキーを設定し、地図を開始してください",28,110,p);}catch(Exception ignored){}finally{if(c!=null)try{surface.unlockCanvasAndPost(c);}catch(Exception ignored){}}}
}
