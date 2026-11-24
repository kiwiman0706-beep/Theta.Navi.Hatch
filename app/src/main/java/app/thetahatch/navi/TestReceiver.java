package app.thetahatch.navi;
import android.content.*;
public class TestReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){
  NaviApp app=(NaviApp)context.getApplicationContext();
  String cmd=intent.getStringExtra("cmd");if(cmd==null)cmd="status";
  if(!app.testMode&&!cmd.equals("enable")&&!cmd.equals("status")&&!cmd.equals("disable")){setResultCode(403);setResultData("test mode is OFF; enable explicitly");return;}
  try{double a=Double.parseDouble(intent.hasExtra("a")?intent.getStringExtra("a"):"1");
   double b=Double.parseDouble(intent.hasExtra("b")?intent.getStringExtra("b"):"0");
   String value=intent.getStringExtra("value");if(value==null)value="";if(value.length()>200)value=value.substring(0,200);
   app.command("adb_simulated",cmd,a,b,value);setResultCode(0);setResultData(app.state().toString());
  }catch(Exception e){setResultCode(400);setResultData(e.toString());}
 }
}
