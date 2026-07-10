Add-Type -AssemblyName System.Drawing

# ---------------------------------------------------------------------------
# Machine block textures for the Extractor (추출로) and Alloy Furnace (합금로).
# Built by recoloring the vanilla furnace / blast-furnace textures (so they read
# as believable furnace-family machines) and adding a distinct accent + glow:
#   Extractor    = stone furnace body recolored steel-blue, CYAN intake glow.
#   Alloy Furnace= blast-furnace metal body recolored warm steel, ORANGE melt glow.
# Each block gets 4 faces: _side, _top, _front, _front_on (lit).
# ---------------------------------------------------------------------------

$root  = 'C:\Projects\mc-extraction\src\main\resources\assets\mcextraction\textures\block'
$vbase = "$PSScriptRoot\vanilla_base"
New-Item -ItemType Directory -Force $root  | Out-Null
New-Item -ItemType Directory -Force $vbase | Out-Null

function Ensure-FurnaceBases {
  $need = @('furnace_side','furnace_top','furnace_front','furnace_front_on',
            'blast_furnace_side','blast_furnace_top','blast_furnace_front','blast_furnace_front_on')
  $missing = $false
  foreach ($n in $need) { if (-not (Test-Path "$vbase\$n.png")) { $missing = $true } }
  if (-not $missing) { return }
  $jar = Get-ChildItem "$env:USERPROFILE\.gradle\caches\neoformruntime" -Recurse -Filter 'minecraft_1.21.1_client.jar' -ErrorAction SilentlyContinue | Select-Object -First 1
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

function Ensure-EnchantBases {
  $need = @('enchanting_table_top','enchanting_table_side','enchanting_table_bottom')
  $missing = $false
  foreach ($n in $need) { if (-not (Test-Path "$vbase\$n.png")) { $missing = $true } }
  if (-not $missing) { return }
  $jar = Get-ChildItem "$env:USERPROFILE\.gradle\caches\neoformruntime" -Recurse -Filter 'minecraft_1.21.1_client.jar' -ErrorAction SilentlyContinue | Select-Object -First 1
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

# Blend a soft radial glow over the whole face center, preserving the underlying
# carving (used for the Rune Altar's lit front — an arcane purple pulse).
function Add-CenterGlow([System.Drawing.Bitmap]$b,[string]$coreHex,[string]$edgeHex){
  $core=Get-RGB $coreHex; $edge=Get-RGB $edgeHex
  $cx=7.5; $cy=7.5; $rad=6.5
  for($y=0;$y -lt 16;$y++){ for($x=0;$x -lt 16;$x++){
    $p=$b.GetPixel($x,$y); if($p.A -eq 0){continue}
    $d=[Math]::Sqrt(($x-$cx)*($x-$cx)+($y-$cy)*($y-$cy))
    if($d -gt $rad){continue}
    $t=[Math]::Max(0.0,1.0-$d/$rad)
    $gr=[int]($edge[0]+($core[0]-$edge[0])*$t)
    $gg=[int]($edge[1]+($core[1]-$edge[1])*$t)
    $gb=[int]($edge[2]+($core[2]-$edge[2])*$t)
    $a=$t*0.6
    $nr=[Math]::Min(255,[int]($p.R+($gr-$p.R)*$a))
    $ng=[Math]::Min(255,[int]($p.G+($gg-$p.G)*$a))
    $nb=[Math]::Min(255,[int]($p.B+($gb-$p.B)*$a))
    $b.SetPixel($x,$y,[System.Drawing.Color]::FromArgb($p.A,$nr,$ng,$nb))
  }}
}

# Draw a small open book (cream pages + cover) centered on a top face — the Rune
# Altar is a full cube, so unlike vanilla it can't show the floating book entity;
# we bake a static one onto the top so the block still reads as an arcane altar.
function Add-Book([System.Drawing.Bitmap]$b){
  $cover=[System.Drawing.Color]::FromArgb(255,120,70,40)
  $spine=[System.Drawing.Color]::FromArgb(255,80,45,25)
  $page =[System.Drawing.Color]::FromArgb(255,232,224,200)
  $shade=[System.Drawing.Color]::FromArgb(255,198,188,162)
  for($x=4;$x -le 11;$x++){ for($y=5;$y -le 10;$y++){ $b.SetPixel($x,$y,$cover) } }   # cover
  for($x=5;$x -le 10;$x++){ for($y=6;$y -le 9;$y++){ $b.SetPixel($x,$y,$page) } }      # pages
  for($y=5;$y -le 10;$y++){ $b.SetPixel(7,$y,$spine); $b.SetPixel(8,$y,$spine) }        # spine
  foreach($x in 5,6,9,10){ $b.SetPixel($x,7,$shade); $b.SetPixel($x,8,$shade) }         # page lines
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

# ---------------- Extractor (추출로): steel-blue + cyan ----------------
$exTint='5E7488'; $exAccent='34C7E0'; $exBase=150.0
$t=Recolor (Load 'furnace_top')   $exTint $exBase $false; Add-Rivets $t $exAccent; Save $t 'extractor_top'; $t.Dispose()
$s=Recolor (Load 'furnace_side')  $exTint $exBase $false; Add-Rivets $s $exAccent; Add-Band $s $exAccent 3; Save $s 'extractor_side'; $s.Dispose()
$fr=Recolor (Load 'furnace_front') $exTint $exBase $false; Add-Rivets $fr $exAccent; Add-Band $fr $exAccent 3; Save $fr 'extractor_front'; $fr.Dispose()
$fo=Recolor (Load 'furnace_front') $exTint $exBase $false; Add-Rivets $fo $exAccent; Add-Band $fo $exAccent 3; Add-Glow $fo 'EAFDFF' '1E9FC0'; Save $fo 'extractor_front_on'; $fo.Dispose()

# ---------------- Alloy Furnace (합금로): warm steel + orange ----------------
$afTint='7A6A5C'; $afAccent='E8922E'; $afBase=140.0
$t=Recolor (Load 'blast_furnace_top')   $afTint $afBase $false; Add-Rivets $t $afAccent; Save $t 'alloy_furnace_top'; $t.Dispose()
$s=Recolor (Load 'blast_furnace_side')  $afTint $afBase $false; Add-Rivets $s $afAccent; Add-Band $s $afAccent 3; Save $s 'alloy_furnace_side'; $s.Dispose()
$fr=Recolor (Load 'blast_furnace_front') $afTint $afBase $false; Add-Rivets $fr $afAccent; Add-Band $fr $afAccent 3; Save $fr 'alloy_furnace_front'; $fr.Dispose()
$fo=Recolor (Load 'blast_furnace_front_on') $afTint $afBase $true; Add-Rivets $fo $afAccent; Add-Band $fo $afAccent 3; Add-Glow $fo 'FFF0C0' 'E0641A'; Save $fo 'alloy_furnace_front_on'; $fo.Dispose()

# ---------------- Rune Altar (룬 제단): enchanting table recolored ARCANE BLUE ----------------
# Vanilla enchanting-table faces recolored to a saturated sapphire (distinct from the extractor's
# muted steel-blue). The block is modeled at the enchanting table's 3/4 height and the floating
# book is drawn by RuneAltarRenderer, so no book is baked and no directional "front" is needed.
Ensure-EnchantBases
$raTint='4A78F0'; $raBase=130.0; $raFloor=0.48   # brighter sapphire; $raFloor lifts the dark obsidian
$t=Recolor-Lift (Load 'enchanting_table_top')    $raTint $raBase $raFloor; Save $t 'rune_altar_top';    $t.Dispose()
$s=Recolor-Lift (Load 'enchanting_table_side')   $raTint $raBase $raFloor; Save $s 'rune_altar_side';   $s.Dispose()
$b=Recolor-Lift (Load 'enchanting_table_bottom') $raTint $raBase $raFloor; Save $b 'rune_altar_bottom'; $b.Dispose()

Write-Output "Generated 8 furnace-family + 3 rune_altar block textures (top/side/bottom) in $root"
