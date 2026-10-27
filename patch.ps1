# Patch BaritoneNavigator.java: remove BOM, add deleteBaritoneCache
$file = "C:\Users\Kitikat\Desktop\AttackDLC\src\main\java\org\attackdlc\pve\navigation\BaritoneNavigator.java"

$bytes = [IO.File]::ReadAllBytes($file)
if ($bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    $bytes = $bytes[3..($bytes.Length - 1)]
}
$text = [Text.Encoding]::UTF8.GetString($bytes)
$lines = $text.Split([char]10)
Write-Output "Total lines: $($lines.Count)"

$found = $false
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match 'requireNonNull\(options') {
        Write-Output "Found requireNonNull at line $($i+1)"
        $lines[$i] = "      deleteBaritoneCache();" + [char]10 + $lines[$i]
        $found = $true
        break
    }
}
if (-not $found) {
    Write-Output "ERROR: requireNonNull not found"
    exit 1
}

$method = [char]10 + "   /** Delete baritone cache. */" + [char]10 +
          "   private static void deleteBaritoneCache() {" + [char]10 +
          "      try {" + [char]10 +
          '         java.nio.file.Path d = java.nio.file.Paths.get("baritone");' + [char]10 +
          "         if (java.nio.file.Files.isDirectory(d)) {" + [char]10 +
          "            java.nio.file.Files.walk(d)" + [char]10 +
          "                   .sorted((a, b) -> b.compareTo(a))" + [char]10 +
          "                   .forEach(p -> { try { java.nio.file.Files.delete(p); } catch (Exception ignored) {} });" + [char]10 +
          "         }" + [char]10 +
          "      } catch (Exception ignored) {}" + [char]10 +
          "   }"

for ($i = $lines.Count - 1; $i -ge 0; $i--) {
    if ($lines[$i].Trim() -eq "}") {
        Write-Output "Inserting method before line $($i+1)"
        $newLines = $lines[0..($i-1)] + $method.Split([char]10) + $lines[$i]
        $lines = $newLines
        break
    }
}

$newText = $lines -join [char]10
$outBytes = [Text.Encoding]::UTF8.GetBytes($newText)
[IO.File]::WriteAllBytes($file, $outBytes)

$verify = [IO.File]::ReadAllText($file, [Text.Encoding]::UTF8)
Write-Output "Has call: $($verify.Contains('deleteBaritoneCache();'))"
Write-Output "Has def: $($verify.Contains('private static void deleteBaritoneCache'))"
Write-Output "Has BOM: $($verify.StartsWith([char]0xFEFF))"
Write-Output "Done!"
