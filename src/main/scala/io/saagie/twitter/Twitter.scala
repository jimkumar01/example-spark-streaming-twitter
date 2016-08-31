package io.saagie.twitter

/**
 * Created by aurelien on 28/08/15.
 */

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.LoggerFactory
import scopt.OptionParser

object Twitter {

  val logger = LoggerFactory.getLogger(this.getClass)

  case class CLIParams(hdfsMaster: String = "", filters: Array[String] = Array.empty)

  def main(args:Array[String]): Unit = {

    val parser = parseArgs("Twitter")

    parser.parse(args, CLIParams()) match {
      case Some(params) =>

        val config = ConfigFactory.load()
        val consumerKey = config.getString("twitter4j.oauth.consumerKey")
        val consumerSecret = config.getString("twitter4j.oauth.consumerSecret")
        val accessToken = config.getString("twitter4j.oauth.accessToken")
        val accessTokenSecret = config.getString("twitter4j.oauth.accessTokenSecret")


        // Set the system properties so that Twitter4j library used by twitter stream
        // can use them to generat OAuth credentials
        System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
        System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
        System.setProperty("twitter4j.oauth.accessToken", accessToken)
        System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

        val sparkConf = new SparkConf().setAppName("TwitterPopularTags")

        System.setProperty("HADOOP_USER_NAME", "hdfs")

        val ssc = new StreamingContext(sparkConf, Seconds(10))

        val stream = TwitterUtils.createStream(ssc, None, params.filters)

        val hashTags = stream.flatMap(tweet => tweet.getText.split(" ").filter(_.startsWith("#")))

        val topHashTags = hashTags.map((_, 1))
          .reduceByKeyAndWindow(_ + _, Seconds(30))
          .map(_.swap)
          .transform(
            _.sortByKey(false)
              .zipWithIndex
              .filter(_._2 < 10)
              .map(_._1)
          )
          .map(_.swap)

        topHashTags.foreachRDD((rdd, ts) => {
          if (!rdd.partitions.isEmpty) {
            rdd.collect().foreach { case (count, tag) => logger.info("%s (%s tweets)".format(tag, count)) }
          }
        })

        topHashTags.saveAsTextFiles(params.hdfsMaster + "/user/hdfs/twitter/")

        ssc.start()
        ssc.awaitTermination()
      case None =>
      // arguments are bad, error message will have been displayed

    }

  }

  def parseArgs(appName: String): OptionParser[CLIParams] = {
    new OptionParser[CLIParams](appName) {
      head(appName, "1.0")
      help("help") text "prints this usage text"

      opt[String]("hdfsMaster") required() action { (data, conf) =>
        conf.copy(hdfsMaster = data)
      } text "URI of hdfs master. Example : hdfs://IP:8020"

      opt[String]("filters") required() action { (data, conf) =>
        conf.copy(filters = conf.filters :+ data)
      } text "Filters to use twitter api. Example : test,twitter"

    }
  }
}
