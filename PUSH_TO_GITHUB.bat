@echo off
chcp 65001 >nul
title Отправка мода в GitHub

echo ========================================================
echo   Отправка The Void Harbinger в ваш репозиторий GitHub
echo ========================================================
echo.
echo Шаг 1: Создайте новый репозиторий на https://github.com/new
echo        (например, с именем: void_harbinger)
echo.

set /p REPO_URL="Вставьте ссылку на ваш репозиторий (или нажмите Enter для void_harbinger): "

if "%REPO_URL%"=="" (
    set "REPO_URL=https://github.com/verysoftermen5637/void_harbinger.git"
)

echo.
echo [1/4] Инициализация Git...
git init
git config user.email "verysoftermen5637@users.noreply.github.com"
git config user.name "verysoftermen5637"
git remote remove origin 2>nul
git remote add origin %REPO_URL%

echo [2/4] Подготовка и коммит файлов...
git add .
git commit -m "The Void Harbinger Mod - Fabric 1.20.1 GeckoLib 4"

echo [3/4] Отправка в ветку main...
git branch -M main
git push -f -u origin main

echo.
echo ========================================================
echo   ГОТОВО! Файлы отправлены в репозиторий!
echo.
echo   Откройте вкладку Actions в репозитории:
echo   %REPO_URL:.git=%/actions
echo.
echo   GitHub автоматически соберет мод за ~1-2 минуты.
echo   Готовый .jar файл скачайте внизу страницы в блоке Artifacts!
echo ========================================================
echo.
pause
