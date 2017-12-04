import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.serializer.KryoSerializer



//case class bits (date: String, Volume: Float, Count:Float, marketcap: Float, price: Float, exchageVol:Float,generatedCoins:Float, fees:Float)

//val coinRDD = bitRDD.map{line=>
//val columns = line.split(",")
//bits(columns(0).toString.replaceAll("/","-"),columns(1).toFloat, columns(2).toFloat, columns(3).toFloat, columns(4).toFloat, columns(5).toFloat, columns(6).toFloat, columns(7).toFloat)
// }

object dataPreProcessor{
  def createSparkContext(): SparkContext = {
    val conf = new SparkConf()
      .setAppName(this.getClass.getSimpleName)
      .set("spark.serializer", classOf[KryoSerializer].getCanonicalName)
    val sc = SparkContext.getOrCreate(conf)
    sc
  }
case class bits (date: String, price: Float, prev_price:Float)
case class tweets (Date:String, user:String, Tweets: String)
def main(args: Array[String]) {
  val sc = createSparkContext()
val sqlCtx = new SQLContext(sc)
import sqlCtx._
import sqlCtx.implicits._
val bitRDD = sc.textFile("fall2017/data/bits.csv")



print("----trial trial trial-----")


var diff = 0.0
var prevprice = 0.0
val coinRDD = bitRDD.map{line=>

val slit = line.split(",")
val diff = slit(4).toFloat-prevprice.toFloat
prevprice = slit(4).toFloat
bits(slit(0).toString.replaceAll("/","-"), slit(4).toFloat, diff)

}
                                                                                                                                       

val coinDF = coinRDD.toDF
coinDF.show

val tweetRDD = sc.textFile("fall2017/data/tweets_raw.txt")


val tweet_RDD = tweetRDD.map{line=>
val cols = line.split("\\|\\|")
tweets(cols(2).toString.split("T")(0),cols(0).toString, cols(1).toString.toLowerCase.replaceAll("[\\W]"," "))}

//tweet_RDD.saveAsTextFile("fall2017/data/tweetclean")
val tweetDF = tweet_RDD.toDF
tweetDF.show

print("---------------------table-------------------")

tweetDF.registerTempTable("tweets")

sqlCtx.sql("""Select * from tweets""")

coinDF.registerTempTable("bitcoins")
sqlCtx.sql("""select * from bitcoins""")

sqlCtx.sql("""select bitcoins.date,price,prev_price,Tweets from bitcoins,tweets where bitcoins.date =tweets.Date""").show
}
}
