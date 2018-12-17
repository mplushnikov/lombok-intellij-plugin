@ECHO OFF

SET pluginVersion=0.23

for %%X in (2017.1) do call :buildPlugin %%X
ECHO All Done

echo.&pause&goto:eof

:buildPlugin
SETLOCAL
echo Called with %1
SET IDEA_VERSION=%1
call gradlew clean
call gradlew build || echo ERROR && exit /b
xcopy build\distributions\lombok-plugin-%pluginVersion%.zip distro\lombok-plugin-%pluginVersion%-%1.zip*
ENDLOCAL & SET result=%retval%
