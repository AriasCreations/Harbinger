#!/bin/bash


git --no-pager log --pretty=format:'%aD %an - %s - [%h]' --shortstat > server/src/main/resources/patch.notes