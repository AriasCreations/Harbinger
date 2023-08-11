#!/bin/bash

cd Prebuild
./compile.sh
cd ..
dotnet Prebuild/bootstrap/prebuild.dll /target vs2022 /file Makefile.prebuild