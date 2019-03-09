# community-search
WIP

## Elasticsearch Schema
Elasticsearch version is`6.6.1`.

### Index
```
curl -X PUT http://localhost:9200/chord?pretty -H 'Content-Type: application/json' -d '
{
  "mappings": {
    "community": {
      "properties": {
        "name": {
          "type": "text",
          "analyzer": "standard"
        },
        "desc": {
          "type": "text",
          "analyzer": "standard"
        }
      }
    }
  }
}
'

curl http://localhost:9200/chord?pretty
```

### Document
```
curl -X POST http://localhost:9200/chord/community?pretty -H 'Content-Type: application/json' -d '
{
  "name": "JJUG",
  "desc": "Japan Java User Group"
}
'

curl http://localhost:9200/chord/community/_search?pretty -H 'Content-Type: application/json' -d '
{
  "query": {
    "match_all": {}
  }
}
'
```

## Local Development
### Installation of Elasticsearch
```
brew install elasticsearch
brew info elasticsearch
brew services restart elasticsearch
curl http://localhost:9200
```
