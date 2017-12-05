import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.Row
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.ml.linalg.{Vectors, VectorUDT}
import org.apache.spark.ml.feature.MinMaxScaler
import org.apache.spark.sql.functions.udf
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.sql.functions.udf
import org.apache.spark.mllib.regression.LinearRegressionWithSGD
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.mllib.feature.HashingTF

object Utils {
 val hashingTF = new HashingTF(1000)

 def transformFeatures(tweetText: Seq[String]): Vector = {
   hashingTF.transform(tweetText)
 }
}
val nbModel = NaiveBayesModel.load(sc, "file:///home/chc631/sparkClass/project/btc-stream-predictor/src/main/resources/nbModel")
val lrModel = LinearRegressionModel.load(sc, "file:///home/chc631/sparkClass/project/btc-stream-predictor/src/main/resources/lrModel")
val rawData = spark.read.format("csv").option("header", "true").load("file:///home/chc631/sparkClass/project/btc-stream-predictor/src/main/resources/stream-list-sample.csv")
def getBarebonesTweetText(tweetText: String, stopWordsList: List[String]):Seq[String] = {
   //Remove URLs, RT, MT and other redundant chars / strings from the tweets.
   tweetText.toLowerCase()
     .replaceAll("\n", "")
     .replaceAll("rt\\s+", "")
     .replaceAll("\\s+@\\w+", "")
     .replaceAll("@\\w+", "")
     .replaceAll("\\s+#\\w+", "")
     .replaceAll("#\\w+", "")
     .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
     .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
     .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
     .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
     .split("\\W+")
     .filter(_.matches("^[a-zA-Z]+$"))
     .filter(!stopWordsList.contains(_))
}

val stopWords = sc.textFile("file:/home/chc631/sparkClass/project/btc-stream-predictor/src/main/resources/NLTK_English_Stopwords_Corpus.txt")
val stopWordsList = stopWords.collect.toList
val filteredTweets = rawData.select("text").map{
  case Row(tweet: String) => getBarebonesTweetText(tweet, stopWordsList)
}
filteredTweets.cache()

val tweetScores = filteredTweets.select("value").map{
  case Row(tweet: Seq[String]) => nbModel.predict(Utils.transformFeatures(tweet))
}
import org.apache.spark.sql.functions._
val avgScore = tweetScores.select(avg($"value")).first().getDouble(0)
// val avgScore = -1

import org.apache.spark.mllib.linalg.Vectors
lrModel.predict(Vectors.dense(avgScore))
