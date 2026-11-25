#!/usr/bin/env python3
"""Theta.Navi.Hatch debug APK helper. Python 3 stdlib + Android platform-tools."""
import argparse, datetime, json, pathlib, re, shlex, subprocess, sys, time
PKG='app.thetahatch.navi'
def main():
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument('--serial',required=True,help='Exact adb device serial, e.g. 100.x.y.z:5555')
 p.add_argument('--adb',default='adb')
 sub=p.add_subparsers(dest='op',required=True)
 ins=sub.add_parser('install');ins.add_argument('apk')
 sub.add_parser('vehiclecheck');sub.add_parser('status');sub.add_parser('smoke');sub.add_parser('dualcheck')
 cmd=sub.add_parser('cmd');cmd.add_argument('command',choices=['enable','disable','status','vehicle_probe','zoom','pan','home','overview','north','reset','connect','variant','mark','snapshot','export','export_clear','open_tablet','close_tablet','link','send_car','goto','traffic']);cmd.add_argument('--a',default='1');cmd.add_argument('--b',default='0');cmd.add_argument('--value',default='')
 cap=sub.add_parser('capture');cap.add_argument('--out',default='captures/'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))
 a=p.parse_args();base=[a.adb,'-s',a.serial]
 def adb(*args,binary=False,check=True):
  r=subprocess.run(base+list(args),stdout=subprocess.PIPE,stderr=subprocess.PIPE,timeout=180)
  if check and r.returncode:raise RuntimeError(r.stderr.decode(errors='replace')+r.stdout.decode(errors='replace'))
  return r.stdout if binary else r.stdout.decode('utf-8',errors='replace')
 def shell(*args):return adb('shell',shlex.join(list(args)))
 def command(c,x='1',y='0',value=''):
  out=shell('am','broadcast','-a',PKG+'.TEST','-n',PKG+'/.TestReceiver','--es','cmd',c,'--es','a',str(x),'--es','b',str(y),'--es','value',value)
  if not re.search(r'Broadcast completed: result=0(?:\D|$)',out):raise RuntimeError(out)
  return out
 def state():
  # 0.3.1〜 release は debuggable でないので run-as は使えない。状態は TestReceiver の返事（data="..."）から読む
  out=command('status');m=re.search(r'data="(.*)"\s*$',out.strip(),re.S)
  if not m:raise RuntimeError('status data missing: '+out[:200])
  return json.loads(m.group(1))
 EXPORT='/sdcard/Android/data/'+PKG+'/files/export/'
 def app_file(name):
  # debuggable な版（〜0.3.0、debug ビルド）は run-as、そうでなければ cmd export で書き出した写しを読む
  data=adb('exec-out','run-as',PKG,'cat','files/'+name,binary=True,check=False)
  if data and not data.startswith((b'cat:',b'run-as:')):return data
  data=adb('exec-out','cat',EXPORT+name,binary=True,check=False)
  return b'' if (not data or data.startswith(b'cat:')) else data
 if adb('get-state').strip()!='device':raise RuntimeError('ADB device is not authorized/online')
 if a.op=='install':
  print(adb('install','--no-streaming','-r',str(pathlib.Path(a.apk).resolve())))
  print(shell('am','start','-n',PKG+'/.MainActivity'))
 elif a.op=='status':print(json.dumps(state(),ensure_ascii=False,indent=2))
 elif a.op=='cmd':print(command(a.command,a.a,a.b,a.value))
 elif a.op=='smoke':
  shell('am','start','-n',PKG+'/.MainActivity');command('disable')
  try:command('link',1)
  except RuntimeError as error:
   if 'result=403' not in str(error):raise
  else:raise AssertionError('OFF did not reject mutation')
  command('enable');before=state();command('link',0)
  command('open_tablet');assert state()['tablet_open'] is True
  command('close_tablet');after=state();assert after['tablet_open'] is False
  if before['car_map_ready']:assert before['car_map_generation']==after['car_map_generation'], 'Closing tablet recreated car map'
  for variant in ['full','minimal','map_only']:
   command('variant',value=variant);assert state()['variant']==variant
  command('open_tablet');command('connect',0)
  final=state();command('mark',value='SMOKE_CONTROL_PASS_NOT_VISUAL_PROOF')
  print(json.dumps({'result':'PASS','scope':'control interface only; map rendering, multi-touch and physical dial still require visual testing','sdk_ready':final['navigator_ready'],'car_map_loaded':final['car_map_loaded'],'state':final},ensure_ascii=False,indent=2))
 elif a.op=='vehiclecheck':
  command('enable');command('vehicle_probe',1)
  try:
   for attempt in range(35):
    time.sleep(1);probe=state()['vehicle_location_probe']
    if probe['car']['valid_sample_count']>=3 or probe['car']['status'] in ['UNIMPLEMENTED','HOST_API_BELOW_3','PERMISSION_REQUIRED','PERMISSION_DENIED','WAITING_FOR_HOST']:break
   print(json.dumps({'result':'OBSERVATION_ONLY','note':'API samples do not prove a physical vehicle GPS or better accuracy. Probe stopped after report; use cmd vehicle_probe --a 1 for ongoing collection.','probe':probe},ensure_ascii=False,indent=2))
  finally:command('vehicle_probe',0)
 elif a.op=='dualcheck':
  command('enable');command('link',0)
  before=state()
  if not (before['navigator_ready'] and before['car_map_ready'] and before['car_map_loaded']):raise RuntimeError('Real Android Auto map is not ready/loaded; cannot run dual-view check')
  command('close_tablet');time.sleep(1);closed=state()
  assert closed['car_map_ready'] and closed['car_map_generation']==before['car_map_generation'], 'Car map was lost/recreated'
  assert closed['surface_width']>0 and not closed['tablet_map_ready']
  command('open_tablet')
  for attempt in range(30):
   time.sleep(.5);opened=state()
   if opened['tablet_map_loaded']:break
  else:raise RuntimeError('Tablet map did not report loaded within 15 seconds')
  assert opened['car_map_ready'] and opened['car_map_generation']==before['car_map_generation']
  for key in ['latitude','longitude','zoom','bearing','tilt']:
   assert abs(opened['tablet_camera'][key]-opened['car_camera'][key])<(1e-5 if key in ['latitude','longitude'] else .01), 'Camera mismatch: '+key
  command('mark',value='DUALCHECK_STATE_PASS_VISUAL_CONFIRM_REQUIRED')
  print(json.dumps({'result':'STATE_PASS','scope':'SDK readiness/camera/lifecycle only. Confirm actual car pixels with photo/video; map_loaded alone cannot prove the visible screen stayed intact.','before':before,'closed':closed,'opened':opened},ensure_ascii=False,indent=2))
 elif a.op=='capture':
  out=pathlib.Path(a.out);out.mkdir(parents=True,exist_ok=True)
  current=state();(out/'state.json').write_text(json.dumps(current,ensure_ascii=False,indent=2),encoding='utf-8')
  (out/'tablet-screen.png').write_bytes(adb('exec-out','screencap','-p',binary=True))
  if current['test_mode']:command('export')
  for name in ['events.jsonl','events.previous.jsonl','vehicle-location.jsonl','vehicle-location.previous.jsonl']:
   data=app_file(name)
   if data:(out/name).write_bytes(data)
  (out/'logcat.txt').write_text(adb('logcat','-d','-v','threadtime','ThetaNavi:I','AndroidRuntime:E','*:S'),encoding='utf-8')
  device={name:shell('getprop',prop).strip() for name,prop in {'manufacturer':'ro.product.manufacturer','model':'ro.product.model','android':'ro.build.version.release','sdk':'ro.build.version.sdk'}.items()}
  package_dump=shell('dumpsys','package','com.google.android.projection.gearhead')
  device['android_auto_version_lines']=[line.strip() for line in package_dump.splitlines() if 'versionName=' in line or 'versionCode=' in line]
  (out/'device.json').write_text(json.dumps(device,ensure_ascii=False,indent=2),encoding='utf-8')
  try:
   shell('uiautomator','dump','/sdcard/theta-window.xml')
   (out/'tablet-ui.xml').write_bytes(adb('exec-out','cat','/sdcard/theta-window.xml',binary=True))
  except (RuntimeError,subprocess.TimeoutExpired) as error:
   (out/'capture-errors.txt').write_text('UI hierarchy unavailable: '+str(error),encoding='utf-8')
  if current['test_mode']:
   command('snapshot')
   for name in ['car-map-only-NOT-host-UI.png','tablet-map-only.png']:
    ready=current['car_map_ready'] if name.startswith('car-') else current['tablet_map_ready']
    if not ready:continue
    for attempt in range(20):
     time.sleep(.5);command('export');image=app_file(name)
     if image.startswith(b'\x89PNG'):
      (out/name).write_bytes(image);break
  if current['test_mode']:command('export_clear')
  (out/'README.txt').write_text('tablet-screen.png is the tablet screenshot. car-map-only-NOT-host-UI.png is an SDK snapshot of the car map; it excludes Android Auto bars, buttons, focus, and dashboard. Use a car-screen photo/video or DHU for actual head-unit UI. ADB simulation is not evidence of physical dial input.\n',encoding='utf-8')
  print(str(out.resolve()))
if __name__=='__main__':
 try:main()
 except Exception as e:print('ERROR:',e,file=sys.stderr);sys.exit(1)
