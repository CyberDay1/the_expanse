$ErrorActionPreference="Stop"
function J($p){ (Get-Content $p -Raw) | ConvertFrom-Json -AsHashtable }
$dt="src\main\resources\data\minecraft\dimension_type\overworld.json"
$o=J $dt
if($o.min_y -ne -256 -or $o.height -ne 2288 -or $o.logical_height -ne 2288){ throw "dimension_type incorrect" }
$bad = Get-ChildItem -Recurse -Filter *.json | ? { $_.FullName -match '\\data\\.*\\worldgen\\noise\\' }
if($bad){ throw "Forbidden noise registry files present: `n$($bad.FullName -join "`n")" }
"Sanity OK"
