#!/usr/bin/env bash

comparison_folder="/mnt/c/Users/Christopher Schnick/Documents/Paradox Interactive/Europa Universalis V/screenshots/coa/"
output="../comp.txt"
list="../list.txt"

> "$output"
> "$list"

for f in *.png; do
  if diff -q "$f" "$comparison_folder/$f" > /dev/null;
    then : ;
    else diff=$(compare "$f" "$comparison_folder/$f" -metric RMSE -format "%[distortion]" "info:" 2>&1 | cut -f 1 -d " " );

    if test "a0a" != "a${diff}a" ;
      then echo "${diff}: $f" >> "$output";
    fi;
  fi
done

sum=$(cat "$output" | sed -E 's/([+-]?[0-9.]+)[eE]\+?(-?)([0-9]+)/(\1*10^\2\3)/g'|LC_ALL=C sort -g -r|while read a; do
  if test -n "$a";
  then echo "$a" >> "$list";
  fi;
done ;
echo -n 0 );
echo "$sum"|bc -l ;