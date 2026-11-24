package app.thetahatch.navi;
import org.junit.Test;import static org.junit.Assert.*;
import org.junit.runner.RunWith;import org.robolectric.RobolectricTestRunner;import org.robolectric.annotation.Config;import org.json.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class GeminiSearchTest {
 private JSONObject response(JSONArray chunks)throws Exception{return new JSONObject().put("candidates",new JSONArray().put(new JSONObject().put("content",new JSONObject().put("parts",new JSONArray().put(new JSONObject().put("text","候補です")))).put("groundingMetadata",new JSONObject().put("groundingChunks",chunks))));}
 @Test public void groundedSourceBecomesSelectablePlace()throws Exception{GeminiSearch.Result r=GeminiSearch.parse(response(new JSONArray().put(new JSONObject().put("maps",new JSONObject().put("title","駅").put("uri","https://maps.google.com/?cid=1").put("placeId","places/ChIJabc")))));assertEquals(1,r.places.size());assertEquals("ChIJabc",r.places.get(0).placeId);}
 @Test public void hallucinatedCoordinatesDoNotBecomePins()throws Exception{GeminiSearch.Result r=GeminiSearch.parse(new JSONObject("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Store at 35.1,139.2\"}]}}]}"));assertTrue(r.places.isEmpty());}
 @Test public void untrustedUrisAreNotLaunched()throws Exception{assertFalse(GeminiSearch.safeSource("javascript:alert(1)"));assertFalse(GeminiSearch.safeSource("https://google.com.evil.test/map"));assertTrue(GeminiSearch.safeSource("https://www.google.com/maps/place/x"));}
 @Test public void centerIsOptional()throws Exception{JSONObject yes=GeminiSearch.request("駐車場",CameraState.osaka(),true),no=GeminiSearch.request("京都の店",CameraState.osaka(),false);assertTrue(yes.getJSONObject("toolConfig").getJSONObject("retrievalConfig").has("latLng"));assertFalse(no.getJSONObject("toolConfig").getJSONObject("retrievalConfig").has("latLng"));assertTrue(yes.getJSONArray("tools").getJSONObject(0).has("googleMaps"));}
 @Test public void duplicateSourcesCollapse()throws Exception{JSONObject m=new JSONObject().put("maps",new JSONObject().put("uri","https://www.google.com/maps?cid=1").put("placeId","places/abc"));assertEquals(1,GeminiSearch.parse(response(new JSONArray().put(m).put(m))).places.size());}
}
