#!/usr/bin/env python3
"""Verify this extracted handoff using its SHA-256 manifest (integrity, not trust)."""
import hashlib,pathlib,sys
base=pathlib.Path(__file__).resolve().parents[1]
failed=[];count=0
for line in (base/'MANIFEST_SHA256.txt').read_text(encoding='utf-8').splitlines():
    digest,name=line.split('  ',1);path=(base/name).resolve()
    if not path.is_relative_to(base) or not path.is_file():failed.append(name+': MISSING/INVALID');continue
    h=hashlib.sha256()
    with path.open('rb') as f:
        for data in iter(lambda:f.read(1024*1024),b''):h.update(data)
    if h.hexdigest()!=digest:failed.append(name+': HASH MISMATCH')
    count+=1
print(f'Checked {count} files; {len(failed)} failures')
for failure in failed:print(failure)
sys.exit(bool(failed))
