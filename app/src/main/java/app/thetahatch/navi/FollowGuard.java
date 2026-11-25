package app.thetahatch.navi;

/**
 * Navigation SDK の地図は、走行案内がなくても「現在地を追う」表示へ入り、カメラを動かし続ける（0.3.1 の DHU で約 4 回／秒）。
 * 追う表示のままだと CameraLink の位置合わせがすぐ上書きされるので、案内のない間は追う表示を解く。
 * SDK と取り合いにならないよう、解くのは 10 秒に 5 回まで。
 */
public final class FollowGuard {
 public static final int MAX_CANCELS=5;public static final long WINDOW_MS=10_000;
 private final long[] times=new long[MAX_CANCELS];private int count;
 public int cancelled;public int refused;
 public boolean allow(long nowMs){
  int live=0;for(int i=0;i<count;i++)if(nowMs-times[i]<WINDOW_MS)times[live++]=times[i];count=live;
  if(count>=MAX_CANCELS){refused++;return false;}
  times[count++]=nowMs;cancelled++;return true;
 }
}
