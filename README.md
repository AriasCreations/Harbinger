We are Harbinger
=======

[![In-Dev Publish](https://github.com/AriasCreations/Harbinger/actions/workflows/PublishDocker.yaml/badge.svg)](https://github.com/AriasCreations/Harbinger/actions/workflows/PublishDocker.yaml)


The Harbinger project is the next-gen Aria's Creations server and script suite for product delivery and updates.


Setting Up
===
--------

When setting up Harbinger, the default port of 7767 for TCP/HTTP should be used.

A request should be made to /api to change the PSK.

Compiling
=========
-------

General
----
1. ./prebuild.sh
2. gradle build

For docker
----
1. ./prebuild.sh
2. docker build