# Graylog Kafka Output Plugin


Kafka Output plugin provide the use of 0.9.0.1 client API.

Output plugins is tested only for version >= 0.9.0.1. By default producer implements most-at-once semantics, but
it can be configured throw retries parameters.

So effectively Kafka guarantees at-least-once delivery by default, to implements Exactly-once delivery requires co-operation
with the destination storage, that say configure producer is the first step of configuration.


In The current plugin implementation Kafka Broker balance consumer to
a specific partition so multiple instance of our client can divided
the work of processing records, in the case a consumer fail kafka
broker reassign the partition to other member group. this process is
note as rebalancing the group

**Required Graylog version:** 2.3 and later

Installation
------------

[Download the plugin](https://github.com/Microland/graylog-kafka-output/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

Development
-----------

to accelerate development of new functionality I found very useful  [Graylog project helper](https://github.com/Graylog2/graylog-project)
Putting project on same level of runner module


Usage
-----

The usage is the same input plugin provided by Graylog2

Getting started
---------------

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.
