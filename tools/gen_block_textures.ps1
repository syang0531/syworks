Add-Type -AssemblyName System.Drawing

# ---------------------------------------------------------------------------
# Machine block textures for the Ore Roaster (배소로), Crusher (분쇄기) and Charcoal Kiln (숯가마).
# Built by recoloring the vanilla furnace textures (so they read
# as believable furnace-family machines) and adding a distinct accent + glow:
#   Ore Roaster        = stone furnace body recolored steel-blue, CYAN heat glow.
#   Crusher            = blast-furnace metal body recolored slate gray, AMBER grinding glow.
#   Charcoal Kiln      = fired-clay body, dark bands, EMBER glow (earthy, not metal).
# Each block gets 4 faces: _side, _top, _front, _front_on (lit).
# ---------------------------------------------------------------------------

$root  = Join-Path $PSScriptRoot '..\src\main\resources\assets\syworks\textures\block'
$vbase = "$PSScriptRoot\vanilla_base"
New-Item -ItemType Directory -Force $root  | Out-Null
New-Item -ItemType Directory -Force $vbase | Out-Null

function Ensure-FurnaceBases {
  $need = @('furnace_side','furnace_top','furnace_front','furnace_front_on',
            'blast_furnace_side','blast_furnace_top','blast_furnace_front','blast_furnace_front_on')
  $missing = $false
  foreach ($n in $need) { if (-not (Test-Path "$vbase\$n.png")) { $missing = $true } }
  if (-not $missing) { return }
  $jar = Get-ChildItem "$PSScriptRoot\..\build\moddev\artifacts" -Filter "minecraft-patched-*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch "sources|merged" } | Select-Object -First 1
  if (-not $jar) { throw "Vanilla client jar not found. Run a gradle task once." }
  Add-Type -AssemblyName System.IO.Compression.FileSystem
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
  try {
    foreach ($n in $need) {
      $e = $zip.GetEntry("assets/minecraft/textures/block/$n.png")
      if ($e) { [System.IO.Compression.ZipFileExtensions]::ExtractToFile($e, "$vbase\$n.png", $true) }
    }
  } finally { $zip.Dispose() }
}

function Get-RGB([string]$hex) {
  return @([Convert]::ToInt32($hex.Substring(0,2),16),
           [Convert]::ToInt32($hex.Substring(2,2),16),
           [Convert]::ToInt32($hex.Substring(4,2),16))
}

# Recolor the GRAY (stone/metal) pixels of a base toward $tintHex; keep strongly
# colored pixels (e.g. the orange fire) if $keepColored is set.
function Recolor([System.Drawing.Bitmap]$src, [string]$tintHex, [double]$base, [bool]$keepColored) {
  $t = Get-RGB $tintHex
  $w=$src.Width; $h=$src.Height
  $dst = New-Object System.Drawing.Bitmap $w,$h
  for ($y=0;$y -lt $h;$y++){ for ($x=0;$x -lt $w;$x++){
    $p=$src.GetPixel($x,$y)
    if ($p.A -eq 0){ $dst.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(0,0,0,0)); continue }
    $mx=[Math]::Max($p.R,[Math]::Max($p.G,$p.B)); $mn=[Math]::Min($p.R,[Math]::Min($p.G,$p.B))
    if ($keepColored -and ($mx-$mn) -gt 40) { $dst.SetPixel($x,$y,$p); continue }
    $lum = ($p.R*0.3 + $p.G*0.59 + $p.B*0.11)
    $f = $lum / $base
    $nr=[Math]::Min(255,[int]($t[0]*$f)); $ng=[Math]::Min(255,[int]($t[1]*$f)); $nb=[Math]::Min(255,[int]($t[2]*$f))
    $dst.SetPixel($x,$y,[System.Drawing.Color]::FromArgb($p.A,$nr,$ng,$nb))
  }}
  return $dst
}

# Like a plain multiplicative tint but LIFTS shadows: even the darkest source pixels keep a
# $floor fraction of the tint, so recoloring very dark sources (obsidian) yields a visibly
# colored block instead of near-black. lum=$base maps to full tint; brighter clamps at tint.
function Recolor-Lift([System.Drawing.Bitmap]$src, [string]$tintHex, [double]$base, [double]$floor) {
  $t = Get-RGB $tintHex
  $w=$src.Width; $h=$src.Height
  $dst = New-Object System.Drawing.Bitmap $w,$h
  for ($y=0;$y -lt $h;$y++){ for ($x=0;$x -lt $w;$x++){
    $p=$src.GetPixel($x,$y)
    if ($p.A -eq 0){ $dst.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(0,0,0,0)); continue }
    $lum = ($p.R*0.3 + $p.G*0.59 + $p.B*0.11)
    $v = $floor + (1.0 - $floor) * [Math]::Min(1.0, $lum/$base)
    $nr=[Math]::Min(255,[int]($t[0]*$v)); $ng=[Math]::Min(255,[int]($t[1]*$v)); $nb=[Math]::Min(255,[int]($t[2]*$v))
    $dst.SetPixel($x,$y,[System.Drawing.Color]::FromArgb($p.A,$nr,$ng,$nb))
  }}
  return $dst
}

# Swap exact source colours (RRGGBB hex keys) to target colours; alpha is preserved and any
# cyan diamond gems into amethyst purple while leaving the obsidian/maroon body untouched.
function Load([string]$n) { return New-Object System.Drawing.Bitmap "$vbase\$n.png" }
function Save([System.Drawing.Bitmap]$b,[string]$name){ $b.Save("$root\$name.png",[System.Drawing.Imaging.ImageFormat]::Png) }

# Draw 4 corner rivet pixels in $hex on a bitmap.
function Add-Rivets([System.Drawing.Bitmap]$b,[string]$hex){
  $c=Get-RGB $hex; $col=[System.Drawing.Color]::FromArgb(255,$c[0],$c[1],$c[2])
  foreach($pt in @(@(1,1),@(14,1),@(1,14),@(14,14))){ $b.SetPixel($pt[0],$pt[1],$col) }
}

# A thin accent status band across the upper side of a face.
function Add-Band([System.Drawing.Bitmap]$b,[string]$hex,[int]$y){
  $c=Get-RGB $hex; $col=[System.Drawing.Color]::FromArgb(255,$c[0],$c[1],$c[2])
  for($x=2;$x -le 13;$x++){ $b.SetPixel($x,$y,$col) }
}

# Fill the lower "opening" region with a radial glow (bright center -> accent).
function Add-Glow([System.Drawing.Bitmap]$b,[string]$coreHex,[string]$edgeHex){
  $core=Get-RGB $coreHex; $edge=Get-RGB $edgeHex
  $cx=7.5; $cy=11.0
  for($y=8;$y -le 13;$y++){ for($x=3;$x -le 12;$x++){
    $d=[Math]::Sqrt(($x-$cx)*($x-$cx)+($y-$cy)*($y-$cy))
    if($d -gt 4.2){continue}
    $t=[Math]::Max(0.0,1.0-$d/4.2)
    $r=[int]($edge[0]+($core[0]-$edge[0])*$t)
    $g=[int]($edge[1]+($core[1]-$edge[1])*$t)
    $bl=[int]($edge[2]+($core[2]-$edge[2])*$t)
    $b.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(255,$r,$g,$bl))
  }}
}

Ensure-FurnaceBases

# ---------------- Ore Roaster (배소로): steel-blue + cyan ----------------
$exTint='5E7488'; $exAccent='34C7E0'; $exBase=150.0
$t=Recolor (Load 'furnace_top')   $exTint $exBase $false; Add-Rivets $t $exAccent; Save $t 'ore_roaster_top'; $t.Dispose()
$s=Recolor (Load 'furnace_side')  $exTint $exBase $false; Add-Rivets $s $exAccent; Add-Band $s $exAccent 3; Save $s 'ore_roaster_side'; $s.Dispose()
$fr=Recolor (Load 'furnace_front') $exTint $exBase $false; Add-Rivets $fr $exAccent; Add-Band $fr $exAccent 3; Save $fr 'ore_roaster_front'; $fr.Dispose()
$fo=Recolor (Load 'furnace_front') $exTint $exBase $false; Add-Rivets $fo $exAccent; Add-Band $fo $exAccent 3; Add-Glow $fo 'EAFDFF' '1E9FC0'; Save $fo 'ore_roaster_front_on'; $fo.Dispose()

# ---------------- Crusher (분쇄기): slate gray metal body + amber grinding glow ----------------
# Built on the blast furnace so it reads as a heavier, banded machine than the ore roaster.
$crTint='6A6E78'; $crAccent='D8A33A'; $crBase=140.0
$t=Recolor (Load 'blast_furnace_top')   $crTint $crBase $false; Add-Rivets $t $crAccent; Save $t 'crusher_top'; $t.Dispose()
$s=Recolor (Load 'blast_furnace_side')  $crTint $crBase $false; Add-Rivets $s $crAccent; Add-Band $s $crAccent 3; Save $s 'crusher_side'; $s.Dispose()
$fr=Recolor (Load 'blast_furnace_front') $crTint $crBase $false; Add-Rivets $fr $crAccent; Add-Band $fr $crAccent 3; Save $fr 'crusher_front'; $fr.Dispose()
$fo=Recolor (Load 'blast_furnace_front_on') $crTint $crBase $true; Add-Rivets $fo $crAccent; Add-Band $fo $crAccent 3; Add-Glow $fo 'FFE9B0' 'C4761A'; Save $fo 'crusher_front_on'; $fo.Dispose()

# ---------------- Charcoal Kiln (숯가마): fired-clay body + ember glow ----------------
# Earthy terracotta rather than metal: a real charcoal kiln is a clay-sealed mound, not a machine.
$ckTint='A8613E'; $ckAccent='2E241E'; $ckBase=150.0
$t=Recolor (Load 'furnace_top')   $ckTint $ckBase $false; Add-Rivets $t $ckAccent; Save $t 'charcoal_kiln_top'; $t.Dispose()
$s=Recolor (Load 'furnace_side')  $ckTint $ckBase $false; Add-Rivets $s $ckAccent; Add-Band $s $ckAccent 3; Save $s 'charcoal_kiln_side'; $s.Dispose()
$fr=Recolor (Load 'furnace_front') $ckTint $ckBase $false; Add-Rivets $fr $ckAccent; Add-Band $fr $ckAccent 3; Save $fr 'charcoal_kiln_front'; $fr.Dispose()
$fo=Recolor (Load 'furnace_front') $ckTint $ckBase $false; Add-Rivets $fo $ckAccent; Add-Band $fo $ckAccent 3; Add-Glow $fo 'FFD9A0' 'B33A12'; Save $fo 'charcoal_kiln_front_on'; $fo.Dispose()

Write-Output "Generated 12 machine block textures in $root"
