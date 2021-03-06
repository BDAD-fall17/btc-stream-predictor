import org.apache.spark.sql.SQLContext
val sqlCtx = new SQLContext(sc)
import sqlCtx._
import sqlCtx.implicits._

//read the bitcoin file
val bitRDD = sc.textFile("BDAD/project/data/bits.csv")


// find the price and the difference of todays and yesterdays price
var diff = 0.0
var prevprice = 0.0
val coinRDD = bitRDD.map{line=>
val slit = line.split(",")
val diff = slit(4).toFloat-prevprice.toFloat
prevprice = slit(4).toFloat
(slit(0).toString.replaceAll("/","-"), (slit(4).toFloat, diff))
}



//save as a file
coinRDD.saveAsTextFile("BDAD/project/files/bitcoinsdata.csv")




// create tweet sentimental analysis with data in the format of date, tweet, score

val tweetsenti = sc.textFile("BDAD/project/data/tweets_with_sentiment.txt")

val sentitweet_keyed = tweetsenti.map(line=>line.split("\\|\\|")).map(field =>(field(0).toString.split("T")(0), field(1), field(2)))


//save as file
sentitweet_keyed.saveAsTextFile("BDAD/project/files/sentimental.csv")


//create tweet data grouped by date
val sentitweet_combi = tweetsenti.map(line=>line.split("\\|\\|")).map(field =>(field(0).toString.split("T")(0),(field(1), field(2))))

// group the sentimental data by key
val tkey = sentitweet_combi.groupByKey()

//save as file
tkey.saveAsTextFile("BDAD/project/files/combi.csv")



//combine the bitcoin and the tweet data
val all_combi = tkey.join(coinRDD)

//save as file
all_combi.coalesce(1).saveAsTextFile("BDAD/project/files/senti_score.csv")




//create a new tweet RDD

val tweetscore = tweetsenti.map(line=>line.split("\\|\\|")).map(field =>(field(0).toString.split("T")(0), field(2).toFloat))

val test = coinRDD.join(tweetscore)
test.take(5)

/*

//count the number of tweets
val tweetfreq = tweetscore.map(line=>(line._1,1))
val freqcount = tweetfreq.reduceByKey((v1,v2)=>v1+v2)


//sum all the tweet scores
val count = tweetscore.reduceByKey((v1,v2)=>v1+v2)


//join the date, tweet frequency and score
val tweet_freqcount = count.join(freqcount)

//find the average of the tweet scores per day
val data = tweet_freqcount.map{line=>
val avg = line._2._1/line._2._2
(line._1, avg)
}



//format the data to date, avg score and price diff

val combiiii = coinRDD.join(data)
val combs = combiiii.map(line=>(line._1, line._2._2,line._2._1._2))
combs.take(10)
val combsDF = combs.toDF()
combs.saveAsTextFile("BDAD/project/files/bitdata.csv")
// combsDF.write.format("com.databricks.spark.csv").save("BDAD/project/files/finalfile.csv")


import org.apache.spark.sql.functions._
// df.orderBy(asc("col1"))

combsDF
.orderBy(asc("_1"))
// place all data in a single partition 
.coalesce(1)
.write.format("com.databricks.spark.csv")
.option("header", "false")
.save("BDAD/project/files/finalfile.csv")
*/
