#!/bin/bash
echo "Starting to build meta packages."
for i in $(find . -maxdepth 1 -type d)
do
  if [[ "$i" == "." ]]
  then
    continue
  fi
  conda build ${i##*/} -c conda-forge --override-channels --output-folder build_tmp --no-anaconda-upload

done
echo "Meta packages created. Will be converted for osx-64, linux-64, win-64."

for i in $(find build_tmp -maxdepth 2 -name "*.tar.bz2")
do
  conda convert --platform osx-64 $i -o metapackages
  conda convert --platform linux-64 $i -o metapackages
  conda convert --platform win-64 $i -o metapackages
done

# If current platform is one of the conversion objectives, the conversion did not happen.
# Thus, we move the original packages to the corresponding conversion directory.
subdir=$(conda config --show subdir) # e.g. 'subdir: osx-64'
trimmed_subdir=${subdir##* } # everything after a space, e.g. 'osx-64'
mv "build_tmp/${trimmed_subdir}" metapackages

echo "Cleaning up temporary files."
rm -r build_tmp
conda build purge-all
echo "Finished building meta packages!"