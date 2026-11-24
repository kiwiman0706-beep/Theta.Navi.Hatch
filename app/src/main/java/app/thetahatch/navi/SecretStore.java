package app.thetahatch.navi;
import android.content.Context;
import android.util.Base64;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
/** Device-local keys, encrypted with Android Keystore. Never expose them to diagnostics. */
public final class SecretStore {
 private final Context context;private static final String ALIAS="theta.navi.keys.v2";
 public SecretStore(Context c){context=c;}
 private SecretKey key() throws Exception {KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);if(!ks.containsAlias(ALIAS)){KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();}return (SecretKey)ks.getKey(ALIAS,null);}
 public String read(String name){String raw=context.getSharedPreferences("encrypted_keys",0).getString(name,"");if(raw.isEmpty())return "";try{String[] parts=raw.split(":");Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(parts[0],0)));return new String(c.doFinal(Base64.decode(parts[1],0)),java.nio.charset.StandardCharsets.UTF_8);}catch(Exception e){return "";}}
 public void write(String name,String value) throws Exception {if(!value.isEmpty()&&!value.matches("[A-Za-z0-9_-]{20,200}"))throw new IllegalArgumentException("API key format is invalid");if(value.isEmpty()){context.getSharedPreferences("encrypted_keys",0).edit().remove(name).commit();return;}Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());String data=Base64.encodeToString(c.getIV(),2)+":"+Base64.encodeToString(c.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)),2);if(!context.getSharedPreferences("encrypted_keys",0).edit().putString(name,data).commit())throw new java.io.IOException("Key save failed");}
}
