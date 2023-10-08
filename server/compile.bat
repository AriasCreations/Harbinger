@echo off

cd Prebuild
call compile.bat
cd ..

call runprebuild.bat
dotnet build -c Release

cd binaries/updater/release
dotnet Harbinger.Updater -genManifest -updaterMode -manifest ../../manifest.json -host https://ci.zontreck.dev/job/Harbinger%20Server/lastSuccessfulBuild/artifact -plat win
cd ../..
cd release
dotnet Harbinger.Updater -genManifest -manifest ../manifest.json -host https://ci.zontreck.dev/job/Harbinger%20Server/lastSuccessfulBuild/artifact -plat win