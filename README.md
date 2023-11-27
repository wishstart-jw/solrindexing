# Sample Spring Batch Solr Indexing Application

## Sample Data
Data derived from public domain data set here:

https://www.kaggle.com/datasets/manishkc06/electronics-product-pricing-dataset

## Solr Collection
Schema for collection is found in [sample_tech_conf/conf](./tree/file_based/sample_tech_conf/conf)

## Load data
Batch process reads items.csv and loads them into a solr collection

### You can start a local Solr instance with the sample schema using:
docker run -d -p 8983:8983 --name my_solr -v "$PWD/sample_tech_conf:/var/solr/sample_tech_conf" solr solr-precreate sample_tech /var/solr/sample_tech_conf

