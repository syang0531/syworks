# Generates the CurseForge project logo from the machine block textures.
# Output: docs/curseforge/logo.png (512x512). Re-run after changing hero assets.
#   powershell -ExecutionPolicy Bypass -File tools/gen_logo.ps1
Add-Type -AssemblyName System.Drawing

$root   = Split-Path $PSScriptRoot -Parent
$itemTx = Join-Path $root 'src/main/resources/assets/syworks/textures/block'
$outDir = Join-Path $root 'docs/curseforge'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$outPng = Join-Path $outDir 'logo.png'

$S = 512
$bmp = New-Object System.Drawing.Bitmap $S, $S
$g   = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode     = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$g.PixelOffsetMode   = [System.Drawing.Drawing2D.PixelOffsetMode]::Half

# --- Background: dark base + radial slate vignette (machine mood) ---
$g.Clear([System.Drawing.Color]::FromArgb(255, 12, 14, 20))

function Radial-Glow($cx, $cy, $radius, $r, $gr, $b, $alpha) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddEllipse(($cx - $radius), ($cy - $radius), ($radius * 2), ($radius * 2))
    $brush = New-Object System.Drawing.Drawing2D.PathGradientBrush $path
    $brush.CenterColor = [System.Drawing.Color]::FromArgb($alpha, $r, $gr, $b)
    $brush.SurroundColors = @([System.Drawing.Color]::FromArgb(0, $r, $gr, $b))
    $g.FillPath($brush, $path)
    $brush.Dispose(); $path.Dispose()
}

# slate ambient, then a cyan intake glow behind the hero
Radial-Glow 256 250 320  46 60 92   255
Radial-Glow 256 210 190  52 116 140 110

function Draw-Tex($name, $x, $y, $size, $angleDeg) {
    $file = Join-Path $itemTx $name
    if (-not (Test-Path $file)) { Write-Warning "missing $name"; return }
    $img = [System.Drawing.Image]::FromFile($file)
    $state = $g.Save()
    $g.TranslateTransform(($x + $size / 2), ($y + $size / 2))
    if ($angleDeg -ne 0) { $g.RotateTransform($angleDeg) }
    $g.DrawImage($img, (-$size / 2), (-$size / 2), $size, $size)
    $g.Restore($state)
    $img.Dispose()
}

# --- Hero: the three machines, all lit ---
Draw-Tex 'charcoal_kiln_front_on.png' 48  48  170 -12  # behind, left
Draw-Tex 'ore_roaster_front_on.png'   296 56  170 12   # behind, right
Draw-Tex 'crusher_front_on.png'       136 168 240 0    # hero, front and centre

# subtle inner vignette to focus the center
$vig = New-Object System.Drawing.Drawing2D.GraphicsPath
$vig.AddEllipse(-90, -90, ($S + 180), ($S + 180))
$vb = New-Object System.Drawing.Drawing2D.PathGradientBrush $vig
$vb.CenterColor = [System.Drawing.Color]::FromArgb(0, 0, 0, 0)
$vb.SurroundColors = @([System.Drawing.Color]::FromArgb(150, 0, 0, 0))
$g.FillPath($vb, $vig)
$vb.Dispose(); $vig.Dispose()

$g.Dispose()
$bmp.Save($outPng, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
Write-Host "wrote $outPng"
