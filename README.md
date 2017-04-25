# Replication-on-Demand-Cache-Accelerator
This is an optional component associated with the "Replication on Demand" project that populates and maintains a Redis cache for use in increasing the performance of the overall application.  It's main use is caching pre-computed file hashes.

## Installing Redis
The Replication-on-Demand application is deployed to redundant Linux servers running RHEL6.
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
# cd redis-3.2.8
# make 
# make test
# make install
```
* Run the server installation script to establish a Redis server on the local interface running on the default port (6379).
```
# cd utils
# ./install_server.sh
```

## Download and Build the Source
* Pre-requisites:
** JDK v1.8.0 (or higher)
** GIT v1.7.0 (or higher)
** Maven v3.3.8 (or higher)

