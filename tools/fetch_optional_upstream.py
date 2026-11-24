#!/usr/bin/env python3
"""Explicit optional large downloads. Downloads only; NEVER flashes or installs."""
import argparse,hashlib,json,pathlib,sys,urllib.request
base=pathlib.Path(__file__).resolve().parents[1]
items=json.loads((base/'research/OPTIONAL_DOWNLOADS.json').read_text())
p=argparse.ArgumentParser(description=__doc__);p.add_argument('item',choices=items);p.add_argument('--out',required=True,help='Destination directory');a=p.parse_args()
item=items[a.item];dest=pathlib.Path(a.out)/item['filename'];dest.parent.mkdir(parents=True,exist_ok=True)
if dest.exists():p.error('destination already exists; verify or choose a different folder')
part=dest.with_name(dest.name+'.part')
if part.exists():p.error('partial destination already exists; inspect before retry')
h=hashlib.sha256();size=0
try:
    with urllib.request.urlopen(urllib.request.Request(item['url'],headers={'User-Agent':'Theta-Navi-Handoff/1.0'}),timeout=60) as src,part.open('xb') as out:
        while True:
            data=src.read(1024*1024)
            if not data:break
            out.write(data);h.update(data);size+=len(data)
    if size!=item['bytes'] or h.hexdigest()!=item['sha256']:raise ValueError('Size or SHA-256 mismatch; partial retained for inspection')
    part.rename(dest);print('Verified download:',dest)
except Exception as e: print('Download failed:',e,file=sys.stderr);sys.exit(1)
