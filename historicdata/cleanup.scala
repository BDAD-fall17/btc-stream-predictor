import org.apache.spark.sql.SQLContext
val sqlCtx = new SQLContext(sc)
import sqlCtx._
import sqlCtx.implicits._


val tweetstream = sc.textFile("fall2017/files/f.txt")

val t_stream = tweetstream.map(line=>line.split('"')).map(field=>(field(3)
.replaceAll("Jan","01")
.replaceAll("Feb","02")
.replaceAll("Mar","03")
.replaceAll("Apr","04")
.replaceAll("May","05")
.replaceAll("Jun","06")
.replaceAll("Jul","07")
.replaceAll("Aug","08")
.replaceAll("Sep","09")
.replaceAll("Oct","10")
.replaceAll("Nov","11")
.replaceAll("Dec","12"),field(7).toLowerCase.replaceAll("[\\W]"," ")))

//create a file with just the date and tweet
val cleanstream = t_stream.map{line =>
val aa = line._1.split(" ")
val bb = line._2.split("https")
(aa(5)+"-"+aa(1)+"-"+aa(2),bb(0))}

//create a file with the date, time and tweet
val cleanstreamtime = t_stream.map{line =>
val aatime = line._1.split(" ")
val bbtime = line._2.split("https")
(aatime(5)+"-"+aatime(1)+"-"+aatime(2),aatime(3),bbtime(0))}

cleanstream.saveAsTextFile("fall2017/files/cleantweet")
val cleantweetDF = cleanstream.toDF

cleantweetDF.write.format("com.databricks.spark.csv").save("fall2017/files/cleandata")

val cleantweettimeDF = cleanstreamtime.toDF
cleantweettimeDF.write.format("com.databricks.spark.csv").save("fall2017/files/cleantimedata")






//val t_stream = tweetstream.map(line=>line.split("\\|\\|")).map(field =>(field(0).toString.split("T")(0), field(1), field(2)))


//save as file
//sentitweet_keyed.saveAsTextFile("fall2017/files/sentimental.csv")


//create tweet data grouped by date
//val sentitweet_combi = tweetsenti.map(line=>line.split("\\|\\|")).map(field =>(field(0).toString.split("T")(0),(field(1), field(2))))

// group the sentimental data by key
//val tkey = sentitweet_combi.groupByKey()

//save as file
//tkey.saveAsTextFile("fall2017/files/combi.csv")


