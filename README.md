# Stateful Kafka Streams Using Google Managed Kafka

This application processes payments sent by different customers. The message is a key:value pair of CustomerID:DollarAmount. 
The input topic is `payments`. The application processes the stream of payments and performs aggregations per customer. 
The aggregated payments is sinked in the `total.payments` topic. 
The application is built using SpringBoot and Kafka Streams API. 
It demonstrates the stateful streaming capability. 

## Setup
Create a Kafka cluster named `kafka` using Managed Service for Apache Kafka. 
Create the following topics -
* payments
* total.payments

### GKE setup
* Create a GKE cluster. 
* Create GSA `processpayments-gsa@<GCP Project ID>.iam.gserviceaccount.com` 
* Assign the IAM roles listed in [Quickstart Guide](https://docs.cloud.google.com/managed-service-for-apache-kafka/docs/quickstart)
* Assign the IAM roles to use the Artifact Registry and use logging to write log messages 
* Create a Kubernetes SA `processpayments-sa` and set it up as a Workload Identity for the app

## Produce messages
We will use CLI to produce messages. 
You can follow the Managed Service for Apache Kafka [Quickstart Guide](https://docs.cloud.google.com/managed-service-for-apache-kafka/docs/quickstart) to setup the CLI. 
Use the following command to produce messages for `payments` topic - 
`kafka-console-producer.sh   --topic payments   --bootstrap-server $BOOTSTRAP   --producer.config client.properties --property "parse.key=true" --property "key.separator=:"`

## Local execution 
The local execution is done assuming you are running in a VM in the same VPC:Subnet. 
And the default SA account has the IAM permissions to publish messages as listed in the [Quickstart Guide](https://docs.cloud.google.com/managed-service-for-apache-kafka/docs/quickstart).

The application can be executed locally using `./mvnw spring-boot:run` command. 
You need to update the `application.properties` file to change values of the Bootsrap Server URL.

## GKE execution 
* Create a docker image and push it to Artifact Registry 
* Check `gke-manifests.yaml` and ensure the values for ENV vars are correct 
* Deploy the `gke-manifests.yaml` on the GKE
* Check the log messages 
