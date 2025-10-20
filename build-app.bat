@echo off
echo 🚀 PawSync App Build Script
echo ================================

:: Check if we're in the right directory
if not exist "app\build.gradle.kts" (
    echo ❌ Error: Please run this script from the project root directory
    echo    Current directory: %CD%
    echo    Expected files: app\build.gradle.kts
    pause
    exit /b 1
)

echo ✅ Project directory confirmed

:: Try to find JDK installations
echo 🔍 Searching for JDK installations...

:: Check common JDK locations
set JDK_FOUND=0

:: Check Program Files locations
for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVA_HOME=%%i"
        set JDK_FOUND=1
        echo ✅ Found JDK at: %%i
        goto :build
    )
)

for /d %%i in ("C:\Program Files\OpenJDK\jdk-*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVA_HOME=%%i"
        set JDK_FOUND=1
        echo ✅ Found JDK at: %%i
        goto :build
    )
)

for /d %%i in ("C:\Program Files\Java\jdk*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVA_HOME=%%i"
        set JDK_FOUND=1
        echo ✅ Found JDK at: %%i
        goto :build
    )
)

:: Check Android Studio's embedded JDK
for /d %%i in ("C:\Program Files\Android\Android Studio\jbr") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVA_HOME=%%i"
        set JDK_FOUND=1
        echo ✅ Found Android Studio JDK at: %%i
        goto :build
    )
)

:: If no JDK found
if %JDK_FOUND%==0 (
    echo ❌ No JDK found!
    echo.
    echo 📋 Please choose one of these options:
    echo    1. Open this project in Android Studio and build there
    echo    2. Download JDK 17 from: https://adoptium.net/temurin/releases/
    echo    3. Install the JDK and run this script again
    echo.
    echo 💡 Tip: Android Studio (Option 1) is the easiest solution!
    pause
    exit /b 1
)

:build
echo.
echo 🔨 Building PawSync App...
echo Using JDK: %JAVA_HOME%
echo.

:: Set PATH to include JDK
set "PATH=%JAVA_HOME%\bin;%PATH%"

:: Verify javac is available
javac -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: javac not found in JDK path
    echo Please check your JDK installation
    pause
    exit /b 1
)

:: Clean and build
echo 🧹 Cleaning project...
call gradlew clean --no-daemon

if errorlevel 1 (
    echo ❌ Clean failed
    echo.
    echo 💡 Try opening the project in Android Studio instead
    pause
    exit /b 1
)

echo 🔨 Building debug APK...
call gradlew assembleDebug --no-daemon

if errorlevel 1 (
    echo ❌ Build failed
    echo.
    echo 💡 Recommendations:
    echo    1. Open project in Android Studio for easier building
    echo    2. Check the error messages above
    echo    3. Ensure all dependencies are available
    pause
    exit /b 1
)

echo.
echo 🎉 SUCCESS! Your app has been built successfully!
echo.
echo 📱 APK Location: app\build\outputs\apk\debug\app-debug.apk
echo.
echo 🚀 Next steps:
echo    1. Install the APK on your Android device
echo    2. Or run from Android Studio for easier testing
echo.
echo ✅ Your enhanced AnalyzePetHealthUseCase is now ready to run!
pause