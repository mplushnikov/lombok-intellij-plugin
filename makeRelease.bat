@ECHO OFF

SET pluginVersion=0.20

for %%X in (2017.1) do call :buildPlugin %%X

echo.&pause&goto:eof

:buildPlugin
SETLOCALj
echo Called with %1
SET IDEA_VERSION=%1
call gradlew clean
call gradlew build
xcopy build\distributions\lombok-plugin-%pluginVersion%.zip distro\lombok-plugin-%pluginVersion%-%1.zip*
ENDLOCAL & SET result=%retval%
