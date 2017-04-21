# Replication-on-Demand-Cache-Accelerator
This is an optional component associated with the "Replication on Demand" product that populates and maintains a Redis cache for use in increasing the performance of the overall application.

## Installing Redis
The Replication-on-Demand application is deployed to redundant Linux servers running Red Hat el6.
* Install the pre-requisites:
```
# yum install tcl.x86_64
# yum install tcl-devel.x86_64
```
* Download and install Redis from source:
```
# cd /usr/local/src
# wget http://download.redis.io/releases/redis-3.2.8.tar.gz
# tar -zxvf redis-3.2.8.tar.gz
# cd redis-3.2.8.tar.gz
# make 
# make test
# make install
```
