# Локальный запуск клиента Attack DLC (MC 26.2) через инстанс Dimasik
#
# Клиент AttackDLC собирается из исходников (Desktop\AttackDLC) и кладётся
# в хранилище клиентов вместо мода Dimasik: инстанс у них один и тот же
# (MC 26.2 + fabric-api), а папка mods при этом остаётся пустой.
#
# Запуск:
#   1) один раз в лаунчере нажать «Открыть» у карточки Dimasik
#      (скачается инстанс MC 26.2, клиент уедет в хранилище);
#   2) запустить этот скрипт;
#   3) в лаунчере снова нажать «Открыть» у карточки Dimasik.
#
# ВАЖНО: у карточки Dimasik не нажимать «Проверить обновления» — иначе
# лаунчер удалит наш мод (его нет в репозитории Dimasik26) и вернёт
# dimasik-0.1-dev.jar. После этого достаточно запустить скрипт заново.

$ErrorActionPreference = 'Stop'

$build = 'C:\Users\Kitikat\Desktop\AttackDLC\build\libs'
$store = Join-Path $env:LOCALAPPDATA 'AttackDLC\store\Dimasik26'

$jar = Get-ChildItem -Path $build -Filter 'attackdlc-*.jar' -File -ErrorAction SilentlyContinue |
   Where-Object { $_.Name -notlike '*-sources.jar' } |
   Sort-Object LastWriteTime -Descending |
   Select-Object -First 1

if (-not $jar) {
   Write-Host "Сборка клиента не найдена. Собери её:" -ForegroundColor Yellow
   Write-Host "  cd C:\Users\Kitikat\Desktop\AttackDLC"
   Write-Host "  .\gradlew.bat build"
   exit 1
}

if (-not (Test-Path $store)) {
   Write-Host "Инстанс Dimasik ещё не установлен." -ForegroundColor Yellow
   Write-Host "Открой в лаунчере карточку Dimasik (кнопка «Открыть») и запусти скрипт заново."
   exit 1
}

Write-Host "Клиент:  $($jar.FullName)  ($([math]::Round($jar.Length/1MB,1)) МБ)"

# старый мод Dimasik убираем — он конфликтует с AttackDLC (HoldMyItems)
Get-ChildItem -Path $store -Filter 'dimasik-*.jar' -File -ErrorAction SilentlyContinue | ForEach-Object {
    Remove-Item -LiteralPath $_.FullName -Force
    Write-Host "  старый мод удалён: $($_.Name)"
}

# наш мод — в хранилище под именем attackdlc-1.0.jar (ожидают ClientStore + Loader)
$target = Join-Path $store 'attackdlc-1.0.jar'
Copy-Item -LiteralPath $jar.FullName -Destination $target -Force
Write-Host "  установлен: attackdlc-1.0.jar" -ForegroundColor Green

# что теперь лежит в хранилище
Write-Host ""
Write-Host "Хранилище $store :"
Get-ChildItem -Path $store -File | ForEach-Object {
   Write-Host ("  {0,-40} {1,8:N1} МБ" -f $_.Name, ($_.Length/1MB))
}

Write-Host ""
Write-Host "Готово. Теперь в лаунчере: Игры -> Minecraft -> Dimasik -> «Открыть»." -ForegroundColor Green
Write-Host "Клиент подхватит ник и проверку лицензии из файла входа лаунчера."
