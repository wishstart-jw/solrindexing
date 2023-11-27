# Sample Spring Batch Solr Indexing Application

## You can start a local Solr instance with the sample schema using:
docker run -d -p 8983:8983 --name my_solr -v "$PWD/sample_tech_conf:/var/solr/sample_tech_conf" solr solr-precreate sample_tech /var/solr/sample_tech_conf
