Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem

# ---------------------------------------------------------------------------
# GUI panels for the mod's machines. Vanilla-accurate gray panel + recessed slots,
# with the vanilla furnace's empty flame/arrow composited in so the idle state
# looks native (the runtime blitSprite overlays align exactly on top).
#   - alloy_furnace : 3 inputs + fuel + output; flame above fuel, arrow before output.
#   - rune_altar    : base + catalyst + output; no fuel, arrow before output.
# ---------------------------------------------------------------------------

$dir = 'C:\Projects\yame\src\main\resources\assets\yame\textures\gui'
New-Item -ItemType Directory -Force $dir | Out-Null

# Canvas MUST be 256x256 (MC blit assumes 256); GUI content lives in the top-left 176x166.
$CANVAS = 256
$CW = 176; $CH = 166

function C([int]$r,[int]$gg,[int]$b) { return [System.Drawing.Color]::FromArgb(255,$r,$gg,$b) }
$panel  = C 198 198 198
$light  = C 255 255 255
$hole   = C 139 139 139
$border = C 55 55 55

# Load the vanilla furnace.png once (source of the empty flame + arrow graphics).
$furnace = $null
$jar = Get-ChildItem -Path 'C:\Projects\yame\build\moddev' -Recurse -Filter '*client-extra*.jar' -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jar) {
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
  $entry = $zip.GetEntry('assets/minecraft/textures/gui/container/furnace.png')
  if ($entry) {
    $ms = New-Object System.IO.MemoryStream
    $s = $entry.Open(); $s.CopyTo($ms); $s.Close(); $ms.Position = 0
    $furnace = New-Object System.Drawing.Bitmap $ms
  }
  $zip.Dispose()
} else {
  Write-Output "WARN: client-extra jar not found; empty flame/arrow not composited."
}

# Draw a static gray "+" centered at (cx,cy).
function Draw-Plus($g,[int]$cx,[int]$cy){
  $col = New-Object System.Drawing.SolidBrush (C 90 90 90)
  $g.FillRectangle($col, $cx-1, $cy-4, 3, 9)
  $g.FillRectangle($col, $cx-4, $cy-1, 9, 3)
}

# Draw a static gray right-arrow (shaft + head) in a 24x16 box at (x,y).
function Draw-Arrow($g,[int]$x,[int]$y){
  $col = New-Object System.Drawing.SolidBrush (C 90 90 90)
  $g.FillRectangle($col, $x, $y+6, 16, 4)
  $pts = @((New-Object System.Drawing.Point ($x+14),($y+2)),
           (New-Object System.Drawing.Point ($x+23),($y+8)),
           (New-Object System.Drawing.Point ($x+14),($y+14)))
  $g.FillPolygon($col, $pts)
}

# Build one panel. $slots = array of @(x,y) 16x16 content top-lefts. $flame/$arrow =
# @(x,y) dest for the composited vanilla empty flame(14x14)/arrow(24x16), or $null (furnace-style).
# $plus/$staticArrow = @(x,y) for baked static markers (anvil-style), or $null.
function Build-Gui([object[]]$slots, $flame, $arrow, $plus, $staticArrow, [string]$outName) {
  $bmp = New-Object System.Drawing.Bitmap $CANVAS, $CANVAS
  $g = [System.Drawing.Graphics]::FromImage($bmp)
  $g.Clear([System.Drawing.Color]::Transparent)
  $g.FillRectangle((New-Object System.Drawing.SolidBrush $panel), 0, 0, $CW, $CH)

  $penLight = New-Object System.Drawing.Pen $light, 1
  $penDark  = New-Object System.Drawing.Pen $border, 1
  # Outer bevel
  $g.DrawLine($penLight, 0, 0, $CW-1, 0)
  $g.DrawLine($penLight, 0, 0, 0, $CH-1)
  $g.DrawLine($penDark, $CW-1, 0, $CW-1, $CH-1)
  $g.DrawLine($penDark, 0, $CH-1, $CW-1, $CH-1)

  $bHole = New-Object System.Drawing.SolidBrush $hole
  foreach ($sl in $slots) {
    $x = $sl[0]; $y = $sl[1]
    $g.FillRectangle($bHole, $x, $y, 16, 16)
    $g.DrawLine($penDark,  $x-1, $y-1, $x+16, $y-1)
    $g.DrawLine($penDark,  $x-1, $y-1, $x-1, $y+16)
    $g.DrawLine($penLight, $x-1, $y+16, $x+16, $y+16)
    $g.DrawLine($penLight, $x+16, $y-1, $x+16, $y+16)
  }

  # Static anvil-style markers (drawn in normal compositing, before the SourceCopy composites).
  if ($plus)        { Draw-Plus  $g $plus[0] $plus[1] }
  if ($staticArrow) { Draw-Arrow $g $staticArrow[0] $staticArrow[1] }

  # Composite vanilla empty flame (src 56,36,14x14) + empty arrow (src 79,34,24x16).
  if ($furnace) {
    $g.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    if ($flame) {
      $g.DrawImage($furnace, (New-Object System.Drawing.Rectangle $flame[0],$flame[1],14,14), 56,36,14,14, [System.Drawing.GraphicsUnit]::Pixel)
    }
    if ($arrow) {
      $g.DrawImage($furnace, (New-Object System.Drawing.Rectangle $arrow[0],$arrow[1],24,16), 79,34,24,16, [System.Drawing.GraphicsUnit]::Pixel)
    }
  }

  $g.Dispose()
  $bmp.Save("$dir\$outName.png", [System.Drawing.Imaging.ImageFormat]::Png)
  $bmp.Dispose()
}

# Shared player inventory (3x9) + hotbar slot list.
$inv = @()
for ($row = 0; $row -lt 3; $row++) { for ($col = 0; $col -lt 9; $col++) { $inv += ,@((8 + $col*18), (84 + $row*18)) } }
for ($col = 0; $col -lt 9; $col++) { $inv += ,@((8 + $col*18), 142) }

# Alloy Furnace (matches AlloyFurnaceMenu): 3 inputs + fuel(56,53) + output(116,35).
$afSlots = @(@(30,17),@(30,35),@(30,53),@(56,53),@(116,35)) + $inv
Build-Gui $afSlots @(56,36) @(79,34) $null $null 'alloy_furnace'

# Rune Altar (matches RuneAltarMenu): base(44,35) + catalyst(76,35) + output(134,35).
# Anvil-style: static "+" between inputs and a static arrow before the output (no progress).
$raSlots = @(@(44,35),@(76,35),@(134,35)) + $inv
Build-Gui $raSlots $null $null @(68,43) @(99,34) 'rune_altar'

if ($furnace) { $furnace.Dispose() }
Write-Output "Wrote alloy_furnace.png + rune_altar.png (${CANVAS}x${CANVAS}, content ${CW}x${CH}) to $dir"
