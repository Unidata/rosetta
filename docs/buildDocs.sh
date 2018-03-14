#!/bin/bash
print_opts ()
{
  echo "Usage: buildDoc.sh <options>:" >&2
  echo "    -b : build docs" >&2
  echo "    -s : sync docs to web" >&2
}

while getopts ":bs" opt; do
  options_found=1
  case $opt in
    b)
      echo "...building docs" >&2
      doxygen rosettaDox.config
      cp -vr files dox/html/
      ;;
    s)
      echo "...syncing..." >&2
      # RosettaDocs must be defined as a Host in .ssh/config 
      #   to point to the Unidata webserver for this to work.
      scp index.html RosettaDocs:~/rosetta/
      scp -r dox/html/ RosettaDocs:~/rosetta/docs
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      print_opts
      ;;
  esac
done

if ((!options_found)); then
    print_opts
fi
