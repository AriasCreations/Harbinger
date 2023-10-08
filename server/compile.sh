#!/bin/bash

chmod +x runprebuild.sh
./runprebuild.sh

rm -rf packaged
mkdir packaged
rm -rf binaries/release
mkdir binaries/release

mkdir win
mkdir linux
mkdir mac

HOST="https://ci.zontreck.dev/jobs/Harbinger Server/lastSuccessfulBuild/artifact"


dotnet build -c Release -p:MyRuntimeIdentifier=linux-x64
cd binaries/updater/release
dotnet Harbinger.Updater.dll -plat linux -updaterMode -manifest ../../manifest.json -host $HOST -genManifest
cd ../../release
dotnet Harbinger.Updater.dll -plat linux -manifest ../manifest.json -host $HOST -genManifest
cd ../..
cp -rv binaries/release/* linux/
cp binaries/manifest.json linux/

rm -rf binaries

dotnet build -c Release -p:MyRuntimeIdentifier=win-x64
cd binaries/updater/release
dotnet Harbinger.Updater.dll -plat win -updaterMode -manifest ../../manifest.json -host $HOST -genManifest
cd ../../release
dotnet Harbinger.Updater.dll -plat win -manifest ../manifest.json -host $HOST -genManifest
cd ../..
cp -rv binaries/release/* win/
cp binaries/manifest.json win/

rm -rf binaries

dotnet build -c Release -p:MyRuntimeIdentifier=osx-x64
cd binaries/updater/release
dotnet Harbinger.Updater.dll -plat mac -updaterMode -manifest ../../manifest.json -host $HOST -genManifest
cd ../../release
dotnet Harbinger.Updater.dll -plat mac -manifest ../manifest.json -host $HOST -genManifest
cd ../..
cp -rv binaries/release/* mac/
cp binaries/manifest.json mac/