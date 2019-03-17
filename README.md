# community-search
docs: http://localhost:8080/docs/index.html

## Elasticsearch
Elasticsearch version is`6.6.1`.

## Local Development
### Installation of Elasticsearch
```
brew install elasticsearch
brew info elasticsearch
brew services restart elasticsearch
curl http://localhost:9200

/usr/local/Cellar/elasticsearch/6.6.1/bin/elasticsearch-plugin install analysis-icu
/usr/local/Cellar/elasticsearch/6.6.1/bin/elasticsearch-plugin install analysis-kuromoji
```
