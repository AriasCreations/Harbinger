@echo off

cd Prebuild
call compile.bat
cd ..

call runprebuild.bat
dotnet build -c Release