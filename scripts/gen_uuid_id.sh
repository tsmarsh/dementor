#!/bin/bash

# Check if the input file is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <input-file.csv>"
  exit 1
fi


input_file="$1"

# Print the header with the new ID column
echo "ID, $(head -n 1 "$input_file")"

# Iterate through the rest of the file, skipping the header
tail -n +2 "$input_file" | while IFS= read -r line; do
  # Generate a UUID and print it along with the original line
  uuid=$(uuidgen)
  echo "$uuid, $line"
done
