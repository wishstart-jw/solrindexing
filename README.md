# Sample Spring Batch Solr Indexing Application

## Overview
Getting data into a Solr collection can be a tricky process. However, building your own system to do so, allows for great flexibility in both the process and final result.
Here we are presenting a sample indexing application built with [Spring Batch,](https://spring.io/projects/spring-batch) as a demo for how this can be done.
**Note:** If you want to see a simplified version of the project look back at some of the initial commits where I demonstrate the basic technique with less abstraction and re-use. Also note, comments in the code indicating potential extension points.

## Sample Data
An embedded h2 database in included to run the sample app.
The data included is derived from public domain data set found on here:
https://www.kaggle.com/datasets/manishkc06/electronics-product-pricing-dataset
It represents a collection of technology products similar in concept to the Solr native tech products collection.

## Solr Collection
Schema for collection is found in [sample_tech_conf/conf](./tree/file_based/sample_tech_conf/conf)

## Basic steps

 1. Clear the existing data from the Solr collection. (This is not a good thing to do in a production environment. Instead build a second collection in parallel and use aliases to switch between the two collections.)
 2. Load this initial list of documents /products. This will allow you to add data from other tables without being overly concerned about adding data later to documents that are not expected to be part of the index.
 3. Load supplemental data to the previously created documents. You can observe some interesting techniques here like stacking values to create a multi valued facet.

## Running the app
#### Start an instance of Solr locally
You can start a local docker container with a Solr instance with the sample schema using:
docker run -d -p 8983:8983 --name my_solr -v "$PWD/sample_tech_conf:/var/solr/sample_tech_conf" solr solr-precreate sample_tech /var/solr/sample_tech_conf
#### Build the project using Maven
#### Run the resulting jar file

