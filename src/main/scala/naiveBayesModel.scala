import org.apache.spark._
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.sql._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType,DoubleType};
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vector


val customSchema_1 = StructType(Array(
    StructField("TIME", StringType, true),
    StructField("tweet", StringType, true),
    StructField("score", StringType, true))
);


//val rawData = sc.textFile("file:///./btc-stream-predictor/src/main/resources/tweets_with_sentiment.txt")
val rawData = sc.textFile("file:/Users/beacon/Classes/Sem03-BDAD/project/btc-stream-predictor/src/main/resources/tweets_with_sentiment.txt")
rawData.take(1)
val rowRDD = rawData.map(line => Row.fromSeq(line.split("\\|\\|")))
val tweetsDF = spark.createDataFrame(rowRDD, customSchema_1)
// tweetsDF.withColumn("score", $"score".cast(DoubleType))
// val tweetsDF2 = tweetsDF.withColumn("scoreTmp", tweetsDF.score.cast(DoubleType))
//     .drop("score")
//     .withColumnRenamed("scoreTmp", "score")
val tweetsDF2 = tweetsDF.selectExpr("TIME", 
                        "tweet",
                        "cast(score as double) score" 
                        )

def getBarebonesTweetText(tweetText: String, stopWordsList: List[String]): Seq[String] = {
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

object Utils {
  val hashingTF = new HashingTF(1000)

  def transformFeatures(tweetText: Seq[String]): Vector = {
    hashingTF.transform(tweetText)
  }
}

// import com.typesafe.config.{Config, ConfigFactory}
object PropertiesLoader {
  // private val conf: Config = ConfigFactory.load("application.conf")
  // val nltkStopWords = conf.getString("NLTK_STOPWORDS_FILE_NAME ")
  val nltkStopWords = "NLTK_English_Stopwords_Corpus.txt"
}
//val stopWords = sc.textFile("file:///./btc-stream-predictor/src/main/resources/NLTK_English_Stopwords_Corpus.txt")
val stopWords = sc.textFile("file:/Users/beacon/Classes/Sem03-BDAD/project/btc-stream-predictor/src/main/resources/NLTK_English_Stopwords_Corpus.txt")
val stopWordsList = stopWords.collect.toList

val labeledRDD = tweetsDF2.select("score", "tweet").rdd.map {
  case Row(score: Double, tweet: String) =>
  val tweetInWords: Seq[String] = getBarebonesTweetText(tweet, stopWordsList)
  LabeledPoint(score, Utils.transformFeatures(tweetInWords))
}
labeledRDD.cache()

val naiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
// naiveBayesModel.save(sc, PropertiesLoader.naiveBayesModelPath)
naiveBayesModel.save(sc, "target/tmp/NaiveBayesModel")

val test = getBarebonesTweetText("bitcoin is so good i should buy but i am shit at buying", stopWordsList)

naiveBayesModel.predict(Utils.transformFeatures(test))

