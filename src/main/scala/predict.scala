import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.regression.LinearRegressionModel
import org.apache.spark.mllib.feature.HashingTF

object Utils2 {
 val hashingTF = new HashingTF(1000)

 def transformFeatures(tweetText: Seq[String]): Vector = {
   hashingTF.transform(tweetText)
 }
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
}

object pricePredictor{
  def createSparkContext(): SparkContext = {
    val conf = new SparkConf()
      .setAppName("btc-analyzer")
      //.set("spark.serializer", classOf[KryoSerializer].getCanonicalName)
      .setMaster("local[2]")// .set("spark.executor.memory","1g")
    val sc = SparkContext.getOrCreate(conf)
    sc
  }

  def main(args: Array[String]){
    val sc = createSparkContext()
    val spark = SparkSession
    .builder()
    .appName("btc-analyzer")
    .config("spark.master", "local")
    .getOrCreate()
    import spark.implicits._
    val protocol = "file://"
    val RootDir = "/home/chc631/sparkClass/project/btc-stream-predictor"
    val ResourceDir = "/src/main/resources/"
    val prefix = protocol + RootDir + ResourceDir 
    val nbModel = NaiveBayesModel.load(sc, prefix +"nbModel")
    val lrModel = LinearRegressionModel.load(sc, prefix + "lrModel")
    val rawData = spark.read.format("csv").option("header", "true").load(prefix + "stream-list-sample.csv")
    
    val stopWords = sc.textFile(prefix + "NLTK_English_Stopwords_Corpus.txt")
    val stopWordsList = stopWords.collect.toList
    val filteredTweets = rawData.select("text").map{
      case Row(tweet: String) => Utils2.getBarebonesTweetText(tweet, stopWordsList)
    }
    
    val tweetScores = filteredTweets.select("value").map{
      case Row(tweet: Seq[String]) => nbModel.predict(Utils.transformFeatures(tweet))
    }
    val avgScore = tweetScores.select(avg($"value")).first().getDouble(0)
    // val avgScore = -1
    
    val result = lrModel.predict(Vectors.dense(avgScore))
  }
}

