@echo off
setlocal EnableExtensions DisableDelayedExpansion

rem One-click NeoForge client launcher for Windows.
rem Uses the Gradle wrapper and discovers a Java 21 installation without
rem requiring a machine-specific JAVA_HOME path. Set OHYEAH_OFFLINE=1 to
rem force Gradle offline mode after dependencies have already been cached.

set "PROJECT_ROOT=%~dp0"
set "EXIT_CODE=1"
pushd "%PROJECT_ROOT%" >nul 2>&1
if errorlevel 1 (
    echo [ohyeah] Cannot enter project directory: %PROJECT_ROOT%
    goto :finish
)

if not exist "%PROJECT_ROOT%gradlew.bat" (
    echo [ohyeah] gradlew.bat not found: %PROJECT_ROOT%gradlew.bat
    goto :finish
)

set "JAVA_EXE="
set "JAVA_VALID=0"

if defined JAVA_HOME call :try_java "%JAVA_HOME%\bin\java.exe"

if not defined JAVA_EXE (
    for /f "delims=" %%J in ('where java 2^>nul') do call :try_java "%%~fJ"
)

if not defined JAVA_EXE (
    for /d %%D in (
        "%ProgramFiles%\Microsoft\jdk-21*"
        "%ProgramFiles%\Eclipse Adoptium\jdk-21*"
        "%ProgramFiles%\Java\jdk-21*"
        "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-21*"
        "%USERPROFILE%\.jdks\*"
    ) do call :try_java "%%~fD\bin\java.exe"
)

if not defined JAVA_EXE (
    echo [ohyeah] Java 21 was not found.
    echo [ohyeah] Install Java 21 or set JAVA_HOME to a Java 21 installation.
    goto :finish
)

for %%J in ("%JAVA_EXE%") do set "JAVA_HOME=%%~dpJ.."
set "GRADLE_ARGS=--no-daemon --console=plain"
if /I "%OHYEAH_OFFLINE%"=="1" set "GRADLE_ARGS=%GRADLE_ARGS% --offline"

echo [ohyeah] Project: %PROJECT_ROOT%
echo [ohyeah] Java: %JAVA_HOME%
echo [ohyeah] NeoForge client: online dependency mode
echo [ohyeah] Starting...

call "%PROJECT_ROOT%gradlew.bat" runClient %GRADLE_ARGS%
set "EXIT_CODE=%ERRORLEVEL%"

if "%EXIT_CODE%"=="0" (
    echo [ohyeah] Client exited normally.
) else (
    echo [ohyeah] Client exited with code %EXIT_CODE%.
)

goto :finish

:try_java
if defined JAVA_EXE exit /b 0
if not exist "%~1" exit /b 0
set "JAVA_EXE=%~1"
call :verify_java
if "%JAVA_VALID%"=="0" set "JAVA_EXE="
exit /b 0

:verify_java
set "JAVA_VALID=0"
"%JAVA_EXE%" -version 2>&1 | findstr /C:"21." >nul
if not errorlevel 1 set "JAVA_VALID=1"
exit /b 0

:finish
if not "%EXIT_CODE%"=="0" pause
popd >nul 2>&1
exit /b %EXIT_CODE%
