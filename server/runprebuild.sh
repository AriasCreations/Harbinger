#!/bin/bash

cd Prebuild
chmod +x compile.sh
./compile.sh
cd ..
dotnet Prebuild/bootstrap/prebuild.dll /target vs2022 /file Makefile.prebuild