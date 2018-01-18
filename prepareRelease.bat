@ECHO OFF

SET pluginVersion=0.16

for %%X in (2017.1 2017.1.1 2017.1.2 2017.1.3 2017.1.4 2017.1.5) do call :buildPlugin %%X

:buildPlugin
SETLOCAL
echo Called with %1
SET IDEA_VERSION=%1
call gradlew clean
call gradlew buildPlugin check
copy build\distributions\lombok-plugin-%pluginVersion%.zip distro\lombok-plugin-%pluginVersion%-%1.zip
ENDLOCAL & SET result=%retval%
