package com.theta.navihatch;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.car.app.*;
import androidx.car.app.validation.HostValidator;
public class NaviCarService extends CarAppService {
 @NonNull @Override public HostValidator createHostValidator(){
  if(BuildConfig.DEBUG)return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
  return new HostValidator.Builder(this).addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample).build();
 }
 @NonNull @Override public Session onCreateSession(){return new Session(){
  @NonNull @Override public Screen onCreateScreen(@NonNull Intent intent){
   NaviApp a=(NaviApp)getApplication();a.host=String.valueOf(getHostInfo());
   return new NaviScreen(getCarContext(),a);
  }
 };}
}
