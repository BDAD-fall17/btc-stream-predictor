# btc-stream-predictor

## Purpose
The purpose of this project is to using big data tools to build a near real-time cryptocurrency price predictor

## Dataset
1. Historical Twitter sentiment Data
2. Historical Bitcoin Price
3. Bitcoin Price Live stream
4. Twitter Price Live stream

## WorkFlow
1. Price deltas(nextDay - today) and tweets are joined on date
2. A Naive Bayes model is trained based on historical Twitter Sentiments 

  * given a tweet, the model would output a Double as a score of whether it is bullish or bearish on BTC
3. A regression model is trained based on the day range price deltas and the day's sentiment average 

  * Given a score average, a differential to the next day is predicted
4. Live stream of tweets are first fed to the NBmodel, which output a series of sentiment scores
5. The sentiment scores of a given period would then be averaged and fed to the regression model to output a predicted price change.

## File Description
### NaiveBayesModel.scala
  1. Take a batch of rawtweets, perform data cleaning to leave only english words.
  2. Format the incoming file into a dataframe
  3. Remove stop words
  4. perform labeling on the rows in the dataframe
  5. train a NaiveBayesModel using the labeling points
  6. Simple Test with one tweet
  
### tweet_score.scala
1. Takes the bitcoin historical data to calculate the price deltas (nextDat -Today)
2. Takes the tweet sentimental scored data to calculate the average sentimental score for each day
3. Builds an array of tweets for each day and their respective scores to train the prediction model
4. 

#### Execution
Currently it can be ran in spark-shell as a script:
spark-shell --packages com.databricks:spark-csv_2.10:1.3.0
:load tweet_score.scala
:load btc-stream-predictor/src/main/scala/naiveBayesModel.scala

#### TODO
  1. can't save model for reuse
  2. bigram
  3. filepath currently hardcoded

