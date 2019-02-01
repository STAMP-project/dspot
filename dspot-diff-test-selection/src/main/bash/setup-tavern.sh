#!/usr/bin/env bash

git clone https://github.com/danglotb/tavern.git
cp -r tavern tavern-refactor
cd tavern-refactor
git checkout refactor
cd ..