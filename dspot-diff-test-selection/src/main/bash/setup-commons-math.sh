#!/usr/bin/env bash

# checkout specific version of commons-math
cd commons-math
git checkout bugs-dot-jar_MATH-286_dbdff075
cd ..

# copy commons-math to have a fixed version
cp -r commons-math commons-math_fixed

# apply the dev patch
cd commons-math_fixed
patch -p1 <.bugs-dot-jar/developer-patch.diff
cd ..