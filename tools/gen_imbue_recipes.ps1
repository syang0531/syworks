# Generates every Rune Altar (yame:imbuing) recipe into
#   src/main/resources/data/yame/recipe/
#
# For each vanilla enchantment (see docs/진행상황.md §5.3):
#   Lv1:  <material> + rune  -> enchanted_book{ench:1}
#   LvN:  book{ench:N-1} + rune -> book{ench:N}   (N = 2..max)
# Plus the rune recipe itself (gold ingot + lapis -> rune).
#
# Re-run any time the §5.3 mapping changes. Files are UTF-8 (no BOM) so MC's
# strict JSON parser accepts them. Old book_*/rune.json are overwritten.

$ErrorActionPreference = 'Stop'
$recipeDir = Resolve-Path (Join-Path $PSScriptRoot '..\src\main\resources\data\yame\recipe')
$enc = New-Object System.Text.UTF8Encoding($false)
$castTime = 100

# ench (registry path, minecraft:*) | max level | base material item id
$books = @(
    # --- Armor ---
    @{ ench = 'protection';             max = 4; mat = 'minecraft:iron_ingot' }
    @{ ench = 'fire_protection';        max = 4; mat = 'minecraft:magma_cream' }
    @{ ench = 'blast_protection';       max = 4; mat = 'minecraft:gunpowder' }
    @{ ench = 'projectile_protection';  max = 4; mat = 'minecraft:phantom_membrane' }
    @{ ench = 'feather_falling';        max = 4; mat = 'minecraft:rabbit_foot' }
    @{ ench = 'respiration';            max = 3; mat = 'minecraft:nautilus_shell' }
    @{ ench = 'aqua_affinity';          max = 1; mat = 'minecraft:prismarine_crystals' }
    @{ ench = 'thorns';                 max = 3; mat = 'minecraft:pufferfish' }
    @{ ench = 'depth_strider';          max = 3; mat = 'minecraft:prismarine_shard' }
    @{ ench = 'frost_walker';           max = 2; mat = 'minecraft:blue_ice' }
    @{ ench = 'soul_speed';             max = 3; mat = 'minecraft:soul_soil' }
    @{ ench = 'swift_sneak';            max = 3; mat = 'minecraft:echo_shard' }
    # --- Sword ---
    @{ ench = 'sharpness';              max = 5; mat = 'minecraft:amethyst_shard' }
    @{ ench = 'smite';                  max = 5; mat = 'minecraft:rotten_flesh' }
    @{ ench = 'bane_of_arthropods';     max = 5; mat = 'minecraft:spider_eye' }
    @{ ench = 'knockback';              max = 2; mat = 'minecraft:slime_ball' }
    @{ ench = 'fire_aspect';            max = 2; mat = 'minecraft:blaze_rod' }
    @{ ench = 'looting';                max = 3; mat = 'minecraft:ender_eye' }
    @{ ench = 'sweeping_edge';          max = 3; mat = 'minecraft:sweet_berries' }
    # --- Tools ---
    @{ ench = 'efficiency';             max = 5; mat = 'minecraft:glowstone_dust' }
    @{ ench = 'silk_touch';             max = 1; mat = 'minecraft:cobweb' }
    @{ ench = 'unbreaking';             max = 3; mat = 'minecraft:flint' }
    @{ ench = 'fortune';                max = 3; mat = 'minecraft:emerald' }
    # --- Bow ---
    @{ ench = 'power';                  max = 5; mat = 'minecraft:ghast_tear' }
    @{ ench = 'punch';                  max = 2; mat = 'minecraft:breeze_rod' }
    @{ ench = 'flame';                  max = 1; mat = 'minecraft:blaze_powder' }
    @{ ench = 'infinity';               max = 1; mat = 'minecraft:ender_pearl' }
    # --- Trident / Fishing ---
    @{ ench = 'loyalty';                max = 3; mat = 'minecraft:lead' }
    @{ ench = 'impaling';               max = 5; mat = 'minecraft:pointed_dripstone' }
    @{ ench = 'riptide';                max = 3; mat = 'minecraft:wet_sponge' }
    @{ ench = 'channeling';             max = 1; mat = 'minecraft:lightning_rod' }
    @{ ench = 'luck_of_the_sea';        max = 3; mat = 'minecraft:heart_of_the_sea' }
    @{ ench = 'lure';                   max = 3; mat = 'minecraft:glow_ink_sac' }
    # --- Crossbow ---
    @{ ench = 'multishot';              max = 1; mat = 'minecraft:firework_star' }
    @{ ench = 'quick_charge';           max = 3; mat = 'minecraft:sugar' }
    @{ ench = 'piercing';               max = 4; mat = 'minecraft:quartz' }
    # --- Mace (1.21) ---
    @{ ench = 'density';                max = 5; mat = 'minecraft:heavy_core' }
    @{ ench = 'breach';                 max = 4; mat = 'minecraft:trial_key' }
    @{ ench = 'wind_burst';             max = 3; mat = 'minecraft:wind_charge' }
    # --- Special / Curses ---
    @{ ench = 'mending';                max = 1; mat = 'minecraft:golden_apple' }
    @{ ench = 'binding_curse';          max = 1; mat = 'minecraft:chain' }
    @{ ench = 'vanishing_curse';        max = 1; mat = 'minecraft:fermented_spider_eye' }
)

function Write-Json($name, $content) {
    $path = Join-Path $recipeDir "$name.json"
    [System.IO.File]::WriteAllText($path, $content, $enc)
}

# Book component fragment for a given enchantment + level.
function Book-Stack($ench, $lvl, $indent) {
    $pad = ' ' * $indent
    return @"
{
$pad  "id": "minecraft:enchanted_book",
$pad  "components": {
$pad    "minecraft:stored_enchantments": { "levels": { "minecraft:$ench": $lvl } }
$pad  }
$pad}
"@
}

# --- Rune ---
Write-Json 'rune' @"
{
  "type": "yame:imbuing",
  "base": { "id": "minecraft:gold_ingot" },
  "catalyst": { "id": "minecraft:lapis_lazuli" },
  "result": { "id": "yame:rune" },
  "casttime": $castTime
}
"@

# --- Books ---
$total = 0
foreach ($b in $books) {
    for ($lv = 1; $lv -le $b.max; $lv++) {
        if ($lv -eq 1) {
            $base = "{ `"id`": `"$($b.mat)`" }"
        } else {
            $base = Book-Stack $b.ench ($lv - 1) 2
        }
        $result = Book-Stack $b.ench $lv 2
        $json = @"
{
  "type": "yame:imbuing",
  "base": $base,
  "catalyst": { "id": "yame:rune" },
  "result": $result,
  "casttime": $castTime
}
"@
        Write-Json "book_$($b.ench)_$lv" $json
        $total++
    }
}

Write-Host "Generated $total book recipes + 1 rune recipe = $($total + 1) total, for $($books.Count) enchantments."
