import org.apache.spark._

val raw = sc.textFile("/Users/chenghsi/chchao/NYU/BDAD/btc-stream-predictor/tweetwise_data/tweets_with_sentiment.txt")

def loadTweetsFile(sc: SparkContext, tweetFilePath: String): DataFrame = {
  val sqlContext = SQLContextSingleton.getInstance(sc)
  val tweetsDF = sqlContext.read
    .load(tweetFilePath)
    .toDF()
}

val tweetsDF = loadTweetsFile(sc, raw)

val naiveBayesModel = NaiveBayes.train(labeledRDD, lambda = 1.0, modelType = "multinomial")
naiveBayesModel.save(sc, PropertiesLoader.naiveBayesModelPath)
