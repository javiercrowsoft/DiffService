# DiffService

A simple service that returns a diff between two files

## health-check

```
curl '0.0.0.0:8080/diffservice/admin/health-check'
```

## send a file

```
curl -H'content-type:application/json' -XPUT 'http://0.0.0.0:8080/diffservice/v1/diff/id1/left' -d \
'{
  "name": "foo",
  "data": "xxx"
}'
```

```
curl -H'content-type:application/json' -XPUT 'http://0.0.0.0:8080/diffservice/v1/diff/id1/left' -d@src/it/resources/scala_small.json
```

```
curl -H'content-type:application/json' -XPUT 'http://0.0.0.0:8080/diffservice/v1/diff/id1/left' -d@src/it/resources/scala_big.json
```

## to execute a diff and get the result

```
curl -v 'http://0.0.0.0:8080/diffservice/v1/diff/id1'
```

### response of diff

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
