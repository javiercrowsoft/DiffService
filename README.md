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