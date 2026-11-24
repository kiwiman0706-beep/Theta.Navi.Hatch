package app.thetahatch.navi;

/** Copies camera values only; never transfers or destroys a map View. */
public final class CameraLink {
 public interface Sink {void move(CameraState state);}
 public boolean linked=false;
 public CameraState car=CameraState.osaka(),tablet=CameraState.osaka();
 private Sink carSink,tabletSink;
 public void attach(boolean isCar,Sink sink){if(isCar)carSink=sink;else tabletSink=sink;sink.move(isCar?car:tablet);}
 public void detach(boolean isCar){if(isCar)carSink=null;else tabletSink=null;}
 public void changed(boolean isCar,CameraState state){
  if(isCar)car=state;else tablet=state;
  if(linked&&!state.near(isCar?tablet:car))set(!isCar,state);
 }
 public void set(boolean isCar,CameraState state){if(isCar)car=state;else tablet=state;Sink sink=isCar?carSink:tabletSink;if(sink!=null)sink.move(state);}
 public void openCarOnTablet(){set(false,car);}
 public void sendTabletToCar(){set(true,tablet);}
 public void setLinked(boolean value){linked=value;if(value)openCarOnTablet();}
}
