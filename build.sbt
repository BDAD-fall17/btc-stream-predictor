
javaOptions ++= Seq("-server", "-Xms1536M", "-Xmx1536M", "-XX:+CMSClassUnloadingEnabled")

javacOptions ++= Seq("-server", "-Xms1536M", "-Xmx1536M", "-XX:+CMSClassUnloadingEnabled")

javacOptions ++= Seq("-encoding", "UTF-8")

lazy val btc_stream_predictor = (project in file(".")).
  settings(
    name := "btc-stream-predictor",
    version := "0.1",
    scalaVersion := "2.11.8"
  )

val sparkVersion = "2.2.0"
val sparkCsvVersion = "1.4.0"
val configVersion = "1.3.0"
// val jacksonVersion = "2.8.1"
// val coreNlpVersion = "3.6.0"
// val jedisVersion = "2.9.0"

//assemblyJarName in assembly := "mllib-tweet-sentiment-analysis-assembly-0.1.jar"

// assemblyMergeStrategy in assembly := {
//   case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
//   case PathList("META-INF", xs@_*) => MergeStrategy.discard
//   case x => (assemblyMergeStrategy in assembly).value(x)
// }
assemblyMergeStrategy in assembly := {
 case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % configVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  // "org.apache.spark" %% "spark-streaming-twitter" % sparkVersion,
  "com.databricks" %% "spark-csv" % sparkCsvVersion
  // "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion,
  // "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion classifier "models",
  // "redis.clients" % "jedis" % jedisVersion,
  // "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
)

lazy val defaultSettings = Defaults.coreDefaultSettings ++ Seq(
  resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)

retrieveManaged := false
