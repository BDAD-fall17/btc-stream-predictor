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


val rawData = spark.read.format("csv").option("header", "false").load("file:///home/chc631/sparkClass/project/btc-stream-predictor/src/main/resources/finalfile.csv")
rawData.take(1)

val df = rawData.selectExpr("cast(_c0 as string) time",
                        "cast(_c1 as double) score",
                        "cast(_c2 as double) price_diff"
                        )

val vectorizeCol = udf((v:Double) =>  Vectors.dense(Array(v)))
val df2 = df.withColumn("sVec", vectorizeCol(df("score")))

val scaler = new MinMaxScaler()
.setInputCol("sVec")
.setOutputCol("sScaled")
.setMax(10)
.setMin(-10)

val df3 = scaler.fit(df2).transform(df2)
val vectorHead = udf{ x:DenseVector => x(0) }
val df4 = df3.withColumn("scaledScore", vectorHead(df3("sScaled"))).drop("sVec")
df4.show


val labeledDataset = df4.map(row => 
    new LabeledPoint(
          row.getAs[Double](2), 
          // row.get(4)
          org.apache.spark.mllib.linalg.Vectors.dense(row.getAs[Double](4))
    )
  ).cache()

labeledDataset.take(5)
val labeledRDD : RDD[org.apache.spark.mllib.regression.LabeledPoint] = labeledDataset.rdd.cache
val splits = labeledRDD.randomSplit(Array(0.6, 0.4), seed = 11L)
val training = splits(0).cache()
val test = splits(1)

val numIterations = 1000
val stepSize = 1
val model = LinearRegressionWithSGD.train(training, numIterations, stepSize)

// Evaluate model on training examples and compute training error
val valuesAndPreds = test.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}
val MSE = valuesAndPreds.map{ case(v, p) => math.pow((v - p), 2) }.mean()
println("training Mean Squared Error = " + MSE)

// Save and load model
model.save(sc, "file:///home/chc631/sparkClass/project/btc-stream-predictor/src/main/resources/lrModel")
// model.save(sc, "target/tmp/scalaLinearRegressionWithSGDModel")
// val sameModel = LinearRegressionModel.load(sc, "target/tmp/scalaLinearRegressionWithSGDModel")

val testLabel = LabeledPoint(10.01001, org.apache.spark.mllib.linalg.Vectors.dense(0.3250635798285302))
// val labeledRDD : RDD[org.apache.spark.mllib.regression.LabeledPoint] = labeledDataset.rdd
model.predict(testLabel.features)

