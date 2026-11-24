package app.thetahatch.navi;
import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Search candidates come only from Maps grounding chunks, never model-invented coordinates. */
public final class GeminiSearch {
 public static final class Place {public final String title,uri,placeId;public Place(String t,String u,String id){title=t;uri=u;placeId=id.startsWith("places/")?id.substring(7):id;}}
 public static final class Result {public final String text;public final List<Place> places;public Result(String t,List<Place> p){text=t;places=p;}}
 public static JSONObject request(String query,CameraState center,boolean useCenter)throws JSONException{
  String prompt="Google Mapsを検索して、日本語で候補を最大5件、簡潔に比較してください。施設名・選んだ理由を示し、根拠のない営業時間や座標を推測しないでください。検索依頼: "+query;
  JSONObject r=new JSONObject().put("contents",new JSONArray().put(new JSONObject().put("role","user").put("parts",new JSONArray().put(new JSONObject().put("text",prompt)))))
   .put("tools",new JSONArray().put(new JSONObject().put("googleMaps",new JSONObject())))
   .put("generationConfig",new JSONObject().put("maxOutputTokens",3072));
  JSONObject retrieval=new JSONObject().put("languageCode","ja");if(useCenter)retrieval.put("latLng",new JSONObject().put("latitude",center.latitude).put("longitude",center.longitude));
  return r.put("toolConfig",new JSONObject().put("retrievalConfig",retrieval));
 }
 public static Result parse(JSONObject json)throws JSONException{
  JSONArray candidates=json.optJSONArray("candidates");if(candidates==null||candidates.length()==0)throw new JSONException("回答がありません（フィルタ・入力を確認）");JSONObject c=candidates.getJSONObject(0);
  StringBuilder text=new StringBuilder();JSONObject content=c.optJSONObject("content");if(content!=null){JSONArray parts=content.optJSONArray("parts");if(parts!=null)for(int i=0;i<parts.length();i++){JSONObject part=parts.getJSONObject(i);if(!part.optBoolean("thought",false))text.append(part.optString("text","")).append('\n');}}
  List<Place> out=new ArrayList<>();Set<String> seen=new HashSet<>();JSONObject grounding=c.optJSONObject("groundingMetadata");JSONArray chunks=grounding==null?null:grounding.optJSONArray("groundingChunks");
  if(chunks!=null)for(int i=0;i<chunks.length();i++){JSONObject maps=chunks.getJSONObject(i).optJSONObject("maps");if(maps==null)continue;String uri=maps.optString("uri",""),id=maps.optString("placeId","");if(safeSource(uri)&&seen.add(id.isEmpty()?uri:id))out.add(new Place(maps.optString("title","Google Maps"),uri,id));}
  if(text.length()==0)text.append("回答本文がありません。");if(out.isEmpty())text.append("\nGoogle Mapsの出典付き候補は返りませんでした。この回答から地図のピンは作成しません。");return new Result(text.toString(),out);
 }
 public static boolean safeSource(String s){try{URI u=URI.create(s);String h=u.getHost();return "https".equals(u.getScheme())&&h!=null&&(h.equals("google.com")||h.endsWith(".google.com")||h.equals("google.co.jp")||h.endsWith(".google.co.jp")||h.equals("maps.app.goo.gl"));}catch(Exception e){return false;}}
 public static Result search(String key,String model,String query,CameraState center,boolean useCenter)throws Exception{
  if(!model.matches("[a-zA-Z0-9._-]+"))throw new IllegalArgumentException("モデル名が不正です");
  return parse(http("https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent","POST",key,request(query,center,useCenter),null,null));
 }
 public static double[] locate(String key,String id,String certificate)throws Exception{
  if(!id.matches("[A-Za-z0-9_-]+"))throw new IllegalArgumentException("候補に有効なPlace IDがありません");
  JSONObject p=http("https://places.googleapis.com/v1/places/"+id,"GET",key,null,"id,location",certificate);
  JSONObject l=p.getJSONObject("location");double lat=l.getDouble("latitude"),lng=l.getDouble("longitude");new CameraState(lat,lng,17,0,0);return new double[]{lat,lng};
 }
 private static JSONObject http(String endpoint,String method,String key,JSONObject body,String mask,String certificate)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL(endpoint).openConnection();c.setConnectTimeout(20000);c.setReadTimeout(60000);c.setRequestMethod(method);c.setInstanceFollowRedirects(false);c.setRequestProperty("x-goog-api-key",key);c.setRequestProperty("Accept","application/json");
  if(mask!=null)c.setRequestProperty("X-Goog-FieldMask",mask);
  if(certificate!=null){c.setRequestProperty("X-Android-Package","app.thetahatch.navi");c.setRequestProperty("X-Android-Cert",certificate);}
  try{if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");try(OutputStream o=c.getOutputStream()){o.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
   int status=c.getResponseCode();InputStream input=status>=200&&status<300?c.getInputStream():c.getErrorStream();ByteArrayOutputStream bytes=new ByteArrayOutputStream();if(input!=null)try(InputStream in=input){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(bytes.size()+n>2097152)throw new IOException("応答が大きすぎます");bytes.write(b,0,n);}}
   if(status<200||status>=300)throw new IOException("HTTP "+status+"：APIキー・対象APIの有効化・課金・モデル名・利用制限を確認してください");
   return new JSONObject(bytes.toString("UTF-8"));
  }finally{c.disconnect();}
 }
}
