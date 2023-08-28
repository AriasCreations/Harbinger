@echo off

call compile.bat

cd binaries/Release
Harbinger.RegEdit -file harbinger -set "root/HKS/bots/discord/account" word token "CHANGEME" -flush

echo.
echo.
echo Setup completed.
pause