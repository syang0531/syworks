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

$root  = Join-Path $PSScriptRoot '..\src\main\resources\assets\syalchemy\textures'
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
  #   silver=brightest  chromium=cyan mirror  titanium=dark steel
  tin='B4C2D2'; zinc='93A6B6'; nickel='C4BB9E'; aluminum='CFD5D9'; silver='EAECF0';
  chromium='BAD2DC'; titanium='7C8A9E'; cobalt='3B5DA8'; tungsten='464646'; sulfur='E6D74A';
  # alloys — 4 light silvers get clear casts (gray / cyan / champagne / lavender)
  bronze='CD7F32'; brass='C6A44B'; constantan='B58B5E'; duralumin='CCC3A6'; steel='868C93';
  stainless_steel='AFC6C6'; titanium_alloy='A0A6C6'; tungsten_steel='52555C';
  cobalt_steel='4A5FA0'; electrum='E8D07A';
  extraction_furnace='6E6E6E'; alloy_furnace='5A5A66'
}

# --- Accent dots disabled (users preferred the clean, dot-free look; hue cast
#     alone distinguishes them). Left here so a mark can be re-enabled per item.
$accent = @{}

# --- Vanilla templates to pull from the Minecraft client jar -----------------
$itemTemplates  = @('ingot','sword','pickaxe','axe','shovel','hoe','helmet','chestplate','leggings','boots')
$armorTemplates = @('layer_1','layer_2')

function Ensure-VanillaBases {
  # Already cached?
  $need = $false
  foreach ($t in $itemTemplates)  { if (-not (Test-Path "$vbase\iron_$t.png"))        { $need=$true } }
  foreach ($t in $armorTemplates) { if (-not (Test-Path "$vbase\iron_$t.png"))        { $need=$true } }
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

$metals = 'tin','zinc','nickel','aluminum','silver','chromium','titanium','cobalt','tungsten'
$alloys = 'bronze','brass','constantan','duralumin','steel','stainless_steel','titanium_alloy','tungsten_steel','cobalt_steel','electrum'
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

Write-Output "Recolored $count item textures from vanilla + $($alloys.Count * 2) armor body layers + 2 block textures."
