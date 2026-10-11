@echo off
chcp 65001 >nul
setlocal

set "SDK_DIR=C:\Android-SDK"
set "ADB=%SDK_DIR%\platform-tools\adb.exe"
set "EMULATOR=%SDK_DIR%\emulator\emulator.exe"
set "AVD_NAME=Pixel_8"

echo ========================================================
echo   CHẠY NHANH EDUSCHEDULE TDMU (KHÔNG CẦN ANDROID STUDIO)
echo ========================================================

:: 1. Kiểm tra thiết bị / máy ảo đang chạy
"%ADB%" start-server >nul 2>&1
for /f "skip=1 tokens=1,2" %%a in ('"%ADB%" devices') do (
    if "%%b"=="device" (
        set "DEVICE_FOUND=1"
    )
)

if not defined DEVICE_FOUND (
    echo [1/3] Chưa có máy ảo nào đang bật. Đang khởi động máy ảo %AVD_NAME%...
    start "" "%EMULATOR%" -avd %AVD_NAME% -netdelay none -netspeed full
    echo       Đang chờ máy ảo khởi động xong...
    "%ADB%" wait-for-device
    :wait_boot
    for /f "delims=" %%i in ('"%ADB%" shell getprop sys.boot_completed 2^>nul') do set "BOOTED=%%i"
    if not "%BOOTED%"=="1" (
        timeout /t 2 /nobreak >nul
        goto wait_boot
    )
    echo       Máy ảo đã sẵn sàng!
) else (
    echo [1/3] Đã kết nối thiết bị / máy ảo Android.
)

:: 2. Biên dịch và cài đặt APK mới nhất
echo [2/3] Đang biên dịch và cài đặt ứng dụng (assembleDebug + installDebug)...
call "%~dp0gradlew.bat" installDebug
if errorlevel 1 (
    echo [LỖI] Quá trình biên dịch hoặc cài đặt thất bại!
    pause
    exit /b 1
)

:: 3. Mở ứng dụng trên màn hình máy ảo
echo [3/3] Đang mở ứng dụng EduSchedule trên thiết bị...
"%ADB%" shell am force-stop com.example.schedule_tdmu
"%ADB%" shell am start -n com.example.schedule_tdmu/.LoginActivity

echo ========================================================
echo   HOÀN TẤT! Ứng dụng đã được mở trên màn hình máy ảo.
echo ========================================================
pause
