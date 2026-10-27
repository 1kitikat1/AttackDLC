@echo off
chcp 65001 >nul
set FILE=C:\Users\Kitikat\Desktop\AttackDLC\src\main\java\org\attackdlc\pve\navigation\BaritoneNavigator.java

rem Remove BOM and add deleteBaritoneCache
powershell -NoProfile -Command ^
  "$f='%FILE%'; $b=[IO.File]::ReadAllBytes($f); if($b[0]-eq0xEF -and $b[1]-eq0xBB -and $b[2]-eq0xBF){$b=$b[3..($b.Length-1)]}; $t=[Text.Encoding]::UTF8.GetString($b); $t=$t.Replace('      Objects.requireNonNull(options, `"options`");','      deleteBaritoneCache();`r`n      Objects.requireNonNull(options, `"options`");'); $m='`r`n   private static void deleteBaritoneCache(){`r`n      try{java.nio.file.Path d=java.nio.file.Paths.get(`"baritone`");if(java.nio.file.Files.isDirectory(d)){java.nio.file.Files.walk(d).sorted((a,b)->b.compareTo(a)).forEach(p->{try{java.nio.file.Files.delete(p);}catch(Exception ignored){}});} }catch(Exception ignored){}`r`n'; $i=$t.LastIndexOf('}`r`n'); if($i-gt0){$t=$t.Substring(0,$i)+$m+'`r`n}'}; [IO.File]::WriteAllBytes($f,[Text.Encoding]::UTF8.GetBytes($t)); Write-Output 'done'"
