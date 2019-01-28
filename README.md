# DiffService

A simple service that returns a diff between two files

## health-check

```
curl '0.0.0.0:8080/diffservice/admin/health-check'
```

## Send a file

```
curl -H'content-type:application/json' -XPUT 'http://0.0.0.0:8080/diffservice/v1/diff/id1/left' -d \
'{
  "name": "foo",
  "data": "xxx"
}'

curl -H'content-type:application/json' -XPUT 'http://0.0.0.0:8080/diffservice/v1/diff/id1/right' -d \
'{
  "name": "foo",
  "data": "xxx"
}'

```

## To execute a diff and get the result

```
curl -v 'http://0.0.0.0:8080/diffservice/v1/diff/id1'
```

### Diff response

The service will return a http status code 200 when left and right files are found and a json with the result

```
{
  result: NUMBER,
  description: STRING,
  diffs: [
   {offset: NUMBER, length: NUMBER}, ...
  ]
}
```

Result field will be one of:

- 304 Not modified (identical)
- 409 Conflict (the files aren't equal)

Description will contain one of these sentences:

- The files are identical
- The files's size aren't equal
- The files are different

Diffs field will be empty if files are identical or have different size. 
When the files size is the same diffs will contain a list of tuples with offset and length
for each difference found.

If either left or right files are missing the service will return 404 not found. 
In the description it will state "left|right|both files are not found". In result
it will be:

- 600 when both are missing
- 601 when left is missing
- 602 when right is missing

## Testing

To create json files with base64 encoded data you can use a python script located in folder src/it/resources/scripts. There are some example files in folders images and text in src/it/resources

```
cd src/it/resources/
mkdir base64
python scripts/filetojson.py images/Scala_logo.bmp base64/biga.json
python scripts/filetojson.py images/Scala_logoB.bmp base64/bigb.json
```

Then you can use curl to test

```
curl -H'content-type:application/json' -XPUT 'http://0.0.0.0:8080/diffservice/v1/diff/id1/right' -d@base64/bigb.json
curl -H'content-type:application/json' -XPUT 'http://0.0.0.0:8080/diffservice/v1/diff/id1/left' -d@base64/biga.json
curl -v 'http://0.0.0.0:8080/diffservice/v1/diff/id1'
```

To run test use

```
sbt test
sbt it:test
```

## Future improvements

- Don't wait for diff endpoint to be called. Start the diff of files when both are present.
- Keep files on memory and don't persist them.

