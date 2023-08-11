@echo off

cd Prebuild
call compile.bat
cd ..
dotnet Prebuild/bootstrap/prebuild.dll /target vs2022 /file Makefile.prebuild /p:BuildHash