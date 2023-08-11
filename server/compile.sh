#!/bin/bash

cd Prebuild
./compile.sh
cd ..

rm -rfv packaged || true
mkdir packaged
rm -rf binaries/release
mkdir binaries/release

dotnet build -c Release -p:MyRuntimeIdentifier=linux-x64
cd binaries/release
tar -cvf ../../packaged/linux.tgz ./*

cd ../..
rm -rf binaries/release
mkdir binaries/release

dotnet build -c Release -p:MyRuntimeIdentifier=win-x64
cd binaries/release
tar -cvf ../../packaged/windows.tgz ./*

cd ../..
rm -rf binaries/release
mkdir binaries/release

dotnet build -c Release -p:MyRuntimeIdentifier=osx-x64
cd binaries/release
tar -cvf ../../packaged/osx.tgz ./*