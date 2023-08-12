#!/bin/bash

git clean -xfd
git reset --hard


git submodule foreach --recursive git clean -xfd
git submodule foreach --recursive git reset --hard

git submodule update --recursive --remote --init
