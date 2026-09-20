Add-Type -AssemblyName System.Drawing

# ---------------------------------------------------------------------------
# Recolor-from-vanilla texture generator.
#
# Instead of drawing crude shapes, this tints the *vanilla iron* item textures
# (which are pure neutral grayscale) onto each metal/alloy color. The vanilla
# shape, highlights and shadows are preserved; only the hue changes — exactly
# like copper/gold/iron ingots but in our colors.
#
#   - Ingots  : vanilla iron_ingot,  tinted (+ small corner accent dot for the
#               silvery metals/alloys that would otherwise look identical).
#   - Tools   : vanilla iron_<tool>, tinted only (color, no accent).
#   - Armor   : vanilla iron_<piece>, tinted only.
#   - Body    : vanilla iron_layer_1/2, tinted (worn-armor body texture).
#
# Re-run after adding a metal/alloy: just add its color (+optional accent) below.
# ---------------------------------------------------------------------------

$root  = 'C:\Projects\yame\src\main\resources\assets\yame\textures'
$vbase = "$PSScriptRoot\vanilla_base"   # cached vanilla templates (extracted once)
New-Item -ItemType Directory -Force "$root\item"          | Out-Null
New-Item -ItemType Directory -Force "$root\block"         | Out-Null
New-Item -ItemType Directory -Force "$root\entity\equipment\humanoid"          | Out-Null
New-Item -ItemType Directory -Force "$root\entity\equipment\humanoid_leggings" | Out-Null
New-Item -ItemType Directory -Force $vbase                | Out-Null

# --- Base metal/alloy colors. Distinction is by HUE CAST ONLY (no dots) — the
#     silvery metals/alloys are pushed toward clearly different casts
#     (cool/warm/blue/green/cyan) so they read apart while staying clean. ------
$colors = @{
  # metals — the light-silver cluster spread across distinct casts:
  #   tin=blue  zinc=deep blue-gray  nickel=warm khaki  aluminum=neutral
  #   silver=brightest  chromium=cyan mirror  titanium=dark steel  platinum=warm white
  tin='B4C2D2'; zinc='93A6B6'; nickel='C4BB9E'; aluminum='CFD5D9'; silver='EAECF0';
  chromium='BAD2DC'; titanium='7C8A9E'; cobalt='3B5DA8'; tungsten='464646'; sulfur='E6D74A'; platinum='DCD8CC';
  # alloys — 4 light silvers get clear casts (gray / cyan / champagne / lavender)
  bronze='CD7F32'; brass='C6A44B'; constantan='B58B5E'; duralumin='CCC3A6'; steel='868C93';
  stainless_steel='AFC6C6'; titanium_alloy='A0A6C6'; tungsten_steel='52555C';
  cobalt_steel='4A5FA0'; electrum='E8D07A'; tungsten_carbide='2E3138'; platinum_superalloy='A8E0D0';
  extraction_furnace='6E6E6E'; alloy_furnace='5A5A66'
}

# --- Accent dots disabled (users preferred the clean, dot-free look; hue cast
#     alone distinguishes them). Left here so a mark can be re-enabled per item.
$accent = @{}

# --- Staff colors (§5.7.2). One grayscale staff base is tinted per material,
#     exactly like the swords. The 12 alloys reuse their ingot color above; the 6
#     vanilla materials get their own material-appropriate tint here. ----------
$staffColors = @{
  wooden_staff='9E7B4F'; stone_staff='8A8A8A'; iron_staff='C9C9C9';
  golden_staff='EAC64B'; diamond_staff='6BE0D6'; netherite_staff='4A4247';
  bronze_staff=$colors['bronze']; brass_staff=$colors['brass'];
  constantan_staff=$colors['constantan']; duralumin_staff=$colors['duralumin'];
  steel_staff=$colors['steel']; stainless_steel_staff=$colors['stainless_steel'];
  titanium_alloy_staff=$colors['titanium_alloy']; tungsten_steel_staff=$colors['tungsten_steel'];
  cobalt_steel_staff=$colors['cobalt_steel']; electrum_staff=$colors['electrum'];
  tungsten_carbide_staff=$colors['tungsten_carbide']; platinum_superalloy_staff=$colors['platinum_superalloy']
}

# Spellbooks (§5.7.3): all 10 share one recolored enchanted-book texture — same
# book shape, but the glowing red ribbon is recolored to a glowing blue.
$spellbooks = 'firebolt','frost_arrow','lightning','blizzard','heal','regeneration','haste','shield','poison_cloud','curse'

# --- Vanilla templates to pull from the Minecraft client jar -----------------
$itemTemplates  = @('ingot','sword','pickaxe','axe','shovel','hoe','helmet','chestplate','leggings','boots')
$armorTemplates = @('layer_1','layer_2')

function Ensure-VanillaBases {
  # Already cached?
  $need = $false
  foreach ($t in $itemTemplates)  { if (-not (Test-Path "$vbase\iron_$t.png"))        { $need=$true } }
  foreach ($t in $armorTemplates) { if (-not (Test-Path "$vbase\iron_$t.png"))        { $need=$true } }
  if (-not (Test-Path "$vbase\enchanted_book.png")) { $need=$true }
  if (-not $need) { return }

  $jar = Get-ChildItem "$PSScriptRoot\..\build\moddev\artifacts" -Filter "minecraft-patched-*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch "sources|merged" } | Select-Object -First 1
  if (-not $jar) { throw "Vanilla client jar not found. Run a gradle task (e.g. .\gradlew.bat runData) once so ModDevGradle creates the Minecraft 26.2 artifacts (build/moddev/artifacts)." }
  Add-Type -AssemblyName System.IO.Compression.FileSystem
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
  try {
    foreach ($t in $itemTemplates) {
      $e = $zip.GetEntry("assets/minecraft/textures/item/iron_$t.png")
      if ($e) { [System.IO.Compression.ZipFileExtensions]::ExtractToFile($e, "$vbase\iron_$t.png", $true) }
    }
    # 1.21.2+: worn-armor textures live at entity/equipment/{humanoid,humanoid_leggings}/<material>.png
    $armorEntries = @{ layer_1 = 'assets/minecraft/textures/entity/equipment/humanoid/iron.png'
                       layer_2 = 'assets/minecraft/textures/entity/equipment/humanoid_leggings/iron.png' }
    foreach ($t in $armorTemplates) {
      $e = $zip.GetEntry($armorEntries[$t])
      if ($e) { [System.IO.Compression.ZipFileExtensions]::ExtractToFile($e, "$vbase\iron_$t.png", $true) }
    }
    $eb = $zip.GetEntry("assets/minecraft/textures/item/enchanted_book.png")
    if ($eb) { [System.IO.Compression.ZipFileExtensions]::ExtractToFile($eb, "$vbase\enchanted_book.png", $true) }
  } finally { $zip.Dispose() }
  Write-Output "Extracted vanilla iron templates -> $vbase"
}

function Get-RGB([string]$hex) {
  return @([Convert]::ToInt32($hex.Substring(0,2),16),
           [Convert]::ToInt32($hex.Substring(2,2),16),
           [Convert]::ToInt32($hex.Substring(4,2),16))
}

# Tint a vanilla iron template onto $hex.
#  - FIXED base (same for every template) => a given gray always maps to the
#    same color, so all pieces of one alloy read as the same material (fixes the
#    per-piece tone drift the adaptive median caused).
#  - Only GRAY (metal) pixels are tinted; colored pixels (the brown wood handle
#    on tools) are kept as-is, staying vanilla and letting the metal head's
#    color stand out.
#  - Optional centered accent dot (ingots only).
$TINT_BASE = 150.0     # gray value that maps exactly to the target color
function Tint-Template([string]$srcPath, [string]$hex, [string]$dstPath, [string]$accentHex) {
  $tc = Get-RGB $hex
  $src = New-Object System.Drawing.Bitmap $srcPath
  $w=$src.Width; $h=$src.Height
  $dst = New-Object System.Drawing.Bitmap $w,$h
  for ($y=0;$y -lt $h;$y++){ for ($x=0;$x -lt $w;$x++){
    $p=$src.GetPixel($x,$y)
    if ($p.A -eq 0){ $dst.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(0,0,0,0)); continue }
    $mx=[Math]::Max($p.R,[Math]::Max($p.G,$p.B)); $mn=[Math]::Min($p.R,[Math]::Min($p.G,$p.B))
    if (($mx-$mn) -gt 14) { $dst.SetPixel($x,$y,$p); continue }   # colored (wood handle) -> keep
    $f = $p.R / $TINT_BASE
    $nr=[Math]::Min(255,[int]($tc[0]*$f)); $ng=[Math]::Min(255,[int]($tc[1]*$f)); $nb=[Math]::Min(255,[int]($tc[2]*$f))
    $dst.SetPixel($x,$y,[System.Drawing.Color]::FromArgb($p.A,$nr,$ng,$nb))
  }}

  # accent: 2x2 dot centered on the ingot body (ingots only)
  if ($accentHex) {
    $ac = Get-RGB $accentHex
    $acol = [System.Drawing.Color]::FromArgb(255,$ac[0],$ac[1],$ac[2])
    foreach ($dx in 0,1) { foreach ($dy in 0,1) {
      $ax = 7+$dx; $ay = 8+$dy
      if ($ax -lt $w -and $ay -lt $h -and $src.GetPixel($ax,$ay).A -gt 0) { $dst.SetPixel($ax,$ay,$acol) }
    }}
  }
  $dst.Save($dstPath,[System.Drawing.Imaging.ImageFormat]::Png); $src.Dispose(); $dst.Dispose()
}

# Rune item — custom draw (no vanilla template fits). A dark obsidian tablet with
# gold flecks (금 주괴) and a glowing lapis-cyan glyph (청금석): "금 주괴 + 청금석 → 룬".
function New-RuneIcon([string]$path) {
  $bmp=New-Object System.Drawing.Bitmap 16,16
  $base=[System.Drawing.Color]::FromArgb(255,44,38,68)
  $bevL=[System.Drawing.Color]::FromArgb(255,92,78,140)
  $bevD=[System.Drawing.Color]::FromArgb(255,26,22,44)
  $gold=[System.Drawing.Color]::FromArgb(255,216,180,92)
  $rune=[System.Drawing.Color]::FromArgb(255,120,200,255)
  $core=[System.Drawing.Color]::FromArgb(255,225,242,255)
  # tablet body (x 3..12, y 2..13)
  for($y=2;$y -le 13;$y++){ for($x=3;$x -le 12;$x++){ $bmp.SetPixel($x,$y,$base) } }
  # bevel: light top/left, dark bottom/right
  for($x=3;$x -le 12;$x++){ $bmp.SetPixel($x,2,$bevL); $bmp.SetPixel($x,13,$bevD) }
  for($y=2;$y -le 13;$y++){ $bmp.SetPixel(3,$y,$bevL); $bmp.SetPixel(12,$y,$bevD) }
  # gold corner flecks
  foreach($pt in @(@(4,3),@(11,3),@(4,12),@(11,12))){ $bmp.SetPixel($pt[0],$pt[1],$gold) }
  # glowing angular rune glyph: vertical stroke + upper/lower branches
  foreach($py in 4..11){ $bmp.SetPixel(7,$py,$rune) }
  foreach($pt in @(@(8,5),@(9,4),@(8,6),@(8,9),@(9,10),@(8,10))){ $bmp.SetPixel($pt[0],$pt[1],$rune) }
  foreach($pt in @(@(7,6),@(7,7),@(7,8))){ $bmp.SetPixel($pt[0],$pt[1],$core) }
  $bmp.Save($path,[System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
}

# Staff base — one neutral grayscale hooked wooden staff (shepherd's-crook: a
# curled hook at the top-left, a solid shaft descending to the bottom-right).
# Drawn once and tinted per material like the swords. Fully grayscale so
# Tint-Template recolors all of it. Shades chosen around TINT_BASE=150 so a
# given material color lands on the mid tone.
function New-StaffBase([string]$path) {
  $hi=195; $mid=150; $sh=110; $dk=82
  $bmp = New-Object System.Drawing.Bitmap 16,16
  for($y=0;$y -lt 16;$y++){ for($x=0;$x -lt 16;$x++){ $bmp.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(0,0,0,0)) } }
  function Set-Px($x,$y,$v){ if($x -ge 0 -and $x -lt 16 -and $y -ge 0 -and $y -lt 16){ $bmp.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(255,$v,$v,$v)) } }
  # hook / crook at top-left (open crook, tip curling down)
  Set-Px 4 1 $hi; Set-Px 5 1 $hi; Set-Px 6 1 $mid
  Set-Px 3 2 $hi; Set-Px 7 2 $mid
  Set-Px 3 3 $mid; Set-Px 7 3 $sh
  Set-Px 3 4 $mid; Set-Px 7 4 $sh
  Set-Px 4 4 $dk
  Set-Px 4 5 $sh; Set-Px 5 5 $dk
  # shaft: 2px wide, crook base (7,4) down-right to the foot (12,15)
  $left  = @(@(7,5),@(8,6),@(8,7),@(9,8),@(9,9),@(10,10),@(10,11),@(11,12),@(11,13),@(12,14))
  $right = @(@(8,5),@(9,6),@(9,7),@(10,8),@(10,9),@(11,10),@(11,11),@(12,12),@(12,13),@(13,14))
  foreach($q in $left)  { Set-Px $q[0] $q[1] $mid }
  foreach($q in $right) { Set-Px $q[0] $q[1] $sh }
  # upper-left highlight edge along the shaft
  foreach($q in @(@(7,5),@(8,6),@(8,7),@(9,8),@(9,9),@(10,10),@(10,11),@(11,12),@(11,13))) { Set-Px $q[0] $q[1] $hi }
  # rounded foot
  Set-Px 12 15 $sh; Set-Px 13 15 $dk
  $bmp.Save($path,[System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
}

# Spellbook — recolor the vanilla enchanted book so its glowing red ribbon reads
# as a glowing blue instead (the "purple/red glow -> blue glow" request). Book
# shape, brown cover, gold clasp and white pages are kept; only the 6 ribbon
# reds are remapped onto a blue glow ramp. One texture, shared by all 10 spells.
$spellbookLut = @{
  'C51339'='5AB0FF'; '9D1B37'='2E7BE6'; 'A42C2B'='3579E0';
  '892120'='245FC8'; '6C1717'='1B47A0'; '611414'='163C8E'
}
function New-SpellbookBlue([string]$srcPath,[string]$dstPath) {
  $src = New-Object System.Drawing.Bitmap $srcPath
  $dst = New-Object System.Drawing.Bitmap $src.Width,$src.Height
  for($y=0;$y -lt $src.Height;$y++){ for($x=0;$x -lt $src.Width;$x++){
    $p=$src.GetPixel($x,$y)
    if($p.A -eq 0){ $dst.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(0,0,0,0)); continue }
    $k=('{0:X2}{1:X2}{2:X2}' -f $p.R,$p.G,$p.B)
    if($spellbookLut.ContainsKey($k)){ $c=Get-RGB $spellbookLut[$k]; $dst.SetPixel($x,$y,[System.Drawing.Color]::FromArgb($p.A,$c[0],$c[1],$c[2])) }
    else { $dst.SetPixel($x,$y,$p) }
  }}
  $dst.Save($dstPath,[System.Drawing.Imaging.ImageFormat]::Png); $src.Dispose(); $dst.Dispose()
}

# Block icon (unchanged simple style — not part of the recolor request)
function New-BlockIcon([string]$name,[string]$path) {
  $tc = Get-RGB $colors[$name]
  $col=[System.Drawing.Color]::FromArgb(255,$tc[0],$tc[1],$tc[2])
  $dark=[System.Drawing.Color]::FromArgb(255,[int]($tc[0]*0.6),[int]($tc[1]*0.6),[int]($tc[2]*0.6))
  $mid =[System.Drawing.Color]::FromArgb(255,[int]($tc[0]*0.8),[int]($tc[1]*0.8),[int]($tc[2]*0.8))
  $bmp=New-Object System.Drawing.Bitmap 16,16
  $g=[System.Drawing.Graphics]::FromImage($bmp)
  $g.Clear([System.Drawing.Color]::Transparent)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush $col),0,0,16,16)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush $mid),3,3,10,10)
  $g.DrawRectangle((New-Object System.Drawing.Pen $dark,1),0,0,15,15)
  $g.Dispose(); $bmp.Save($path,[System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose()
}

# ---------------------------------------------------------------------------
Ensure-VanillaBases

$metals = 'tin','zinc','nickel','aluminum','silver','chromium','titanium','cobalt','tungsten','platinum'
$alloys = 'bronze','brass','constantan','duralumin','steel','stainless_steel','titanium_alloy','tungsten_steel','cobalt_steel','electrum','tungsten_carbide','platinum_superalloy'
$gear   = 'sword','pickaxe','axe','shovel','hoe','helmet','chestplate','leggings','boots'

$count = 0

# Metal ingots (+ sulfur) — ingot template, with accent
foreach ($m in $metals) {
  Tint-Template "$vbase\iron_ingot.png" $colors[$m] "$root\item\$($m)_ingot.png" $accent[$m]; $count++
}
Tint-Template "$vbase\iron_ingot.png" $colors['sulfur'] "$root\item\sulfur.png" $accent['sulfur']; $count++

# Alloy ingots (+accent) and full gear set (color only)
foreach ($a in $alloys) {
  Tint-Template "$vbase\iron_ingot.png" $colors[$a] "$root\item\$($a)_ingot.png" $accent[$a]; $count++
  foreach ($t in $gear) {
    Tint-Template "$vbase\iron_$t.png" $colors[$a] "$root\item\$($a)_$t.png" $null; $count++
  }
  # worn-armor body layers (1.21.2+ equipment layout: humanoid = helmet/chest/boots, humanoid_leggings = legs)
  Tint-Template "$vbase\iron_layer_1.png" $colors[$a] "$root\entity\equipment\humanoid\$a.png" $null
  Tint-Template "$vbase\iron_layer_2.png" $colors[$a] "$root\entity\equipment\humanoid_leggings\$a.png" $null
}

# Blocks (unchanged style)
New-BlockIcon 'extraction_furnace' "$root\block\extraction_furnace.png"
New-BlockIcon 'alloy_furnace' "$root\block\alloy_furnace.png"

# Magic system: rune item
New-RuneIcon "$root\item\rune.png"

# Staffs — one grayscale base, tinted per material (like the swords).
New-StaffBase "$vbase\staff.png"
$staffCount = 0
foreach ($w in $staffColors.Keys) {
  Tint-Template "$vbase\staff.png" $staffColors[$w] "$root\item\$($w).png" $null; $staffCount++
}

# Spellbooks — one blue recolor of the enchanted book, written per spell id.
$sbCount = 0
foreach ($s in $spellbooks) {
  New-SpellbookBlue "$vbase\enchanted_book.png" "$root\item\spellbook_$($s).png"; $sbCount++
}

Write-Output "Recolored $count item textures from vanilla + $($alloys.Count * 2) armor body layers + 2 block textures + 1 rune item + $staffCount staffs + $sbCount spellbooks."
