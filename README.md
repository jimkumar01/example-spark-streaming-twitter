In Application properties set your own twitter api credentials: 

twitter4j.oauth.consumerKey=
twitter4j.oauth.consumerSecret=
twitter4j.oauth.accessToken=
twitter4j.oauth.accessTokenSecret=

Package for saagie : sbt clean assembly and get the package in target

To pass args to the program we use scopt. Scopt is a little command line options parsing library.

--hdfsMaster: namenode url hdfs
--filters: filter predicates for tweets

Usage in local :

sbt clean assembly
spark-submit --class=io.saagie.twitter.Twitter example-spark-streaming-twitter-assembly-1.0.jar --hdfsMaster hdfs://hdfshost:8020/ --filters yourFilter1,yourFilter2

Usage in Saagie :

sbt clean assembly (in local, to generate jar file)
create new Spark Job
upload the jar (target/scala-2.10/example-spark-streaming-twitter-assembly-1.0.jar)
replace MyClass with the full class name (ex : io.saagie.twitter.Twitter)
copy URL from HDFS connection details panel and replace arg1 arg2 par --hdfsMaster hdfsUrl --filters yourFilter1,yourFilter2
choose Spark 1.6.1
choose resources you want to allocate to the job
create and launch
