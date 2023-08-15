#!/bin/bash

TABLE_NAME="Questions"
CSV_FILE="path/to/yourfile.csv"

# Read the CSV file line by line
while IFS=, read -r id category question_text
do
  # Create a JSON object for the item
  item=$(cat << EOM
{
  "id": {"S": "$id"},
  "category": {"S": "$category"},
  "question_text": {"S": "$question_text"}
}
EOM
  )

  # Use the AWS CLI to put the item into DynamoDB
  aws dynamodb put-item --table-name $TABLE_NAME --item "$item"
done < $CSV_FILE
