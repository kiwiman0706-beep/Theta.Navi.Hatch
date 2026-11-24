#!/usr/bin/env python3
"""Read-only ADB inventory. No network changes, pairing, root, install or logcat."""
import argparse, datetime, json, pathlib, re, shutil, subprocess, sys, time

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--serial',required=True,help='Explicit ADB target, including port when required')
    p.add_argument('--out',required=True,help='Local JSON report; may contain private device/build information')
    a=p.parse_args()
    if not a.serial.strip(): p.error('serial must not be blank')
    if not shutil.which('adb'): p.error('adb not found; install Android SDK Platform Tools')
    def run(*cmd):
        start=time.monotonic()
        try:
            r=subprocess.run(['adb','-s',a.serial,*cmd],capture_output=True,text=True,timeout=15)
            return {'exit_code':r.returncode,'stdout':r.stdout.strip(),'stderr':r.stderr.strip(),'elapsed_ms':round((time.monotonic()-start)*1000)}
        except subprocess.TimeoutExpired: return {'exit_code':None,'error':'TIMEOUT','elapsed_ms':15000}
    state=run('get-state')
    report={'schema':1,'time_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'expected_device':'ALLDOCUBE iPlay60 Pro (not Pad Pro or Mini Pro); SIM built in (user statement)','connection':state,'scope':'Read-only inventory. Does NOT prove cellular data, VPN, CarPlay, GPS provenance or physical dial operation.'}
    if state.get('stdout')!='device' or state.get('exit_code')!=0:
        report['result']='ADB_NOT_READY';code=2
    else:
        props=['ro.product.manufacturer','ro.product.model','ro.product.device','ro.build.version.release','ro.build.version.sdk','ro.product.cpu.abilist','ro.build.fingerprint']
        report['properties']={k:run('shell','getprop',k) for k in props}
        report['screen']={k:run('shell','wm',k) for k in ('size','density')}
        report['settings']={k:run('shell','settings','get','global',k) for k in ('mobile_data','adb_enabled','adb_wifi_enabled')}
        report['packages']={}
        for package in ('app.thetahatch.navi','io.github.shaonialife.cardeck','moe.shizuku.privileged.api','com.tailscale.ipn','com.google.android.gms'):
            r=run('shell','dumpsys','package',package)
            lines=[l.strip() for l in r.get('stdout','').splitlines() if re.search(r'\b(versionCode=|versionName=|minSdk=|targetSdk=)',l)]
            report['packages'][package]={'exit_code':r.get('exit_code'),'version_lines':lines,'note':'No version lines means absent, restricted or unparsed; check manually.' if not lines else ''}
        report['result']='INVENTORY_COLLECTED';code=0
    dest=pathlib.Path(a.out);dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({'result':report['result'],'report':str(dest)},ensure_ascii=False))
    return code
if __name__=='__main__':sys.exit(main())
