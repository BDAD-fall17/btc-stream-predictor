# btc-stream-predictor

## Purpose
The purpose of this project is to using big data tools to build a near real-time cryptocurrency price predictor

## Dataset
1. Historical Twitter sentiment Data
2. Historical Bitcoin Price
3. Bitcoin Price Live stream
4. Twitter Price Live stream


### WorkFlow
1. Price deltas(nextDay - today) and tweets are joined on date
2. A Naive Bayes model is trained based on historical Twitter Sentiments 
  given a tweet, the model would output a Double as a score of whether it is bullish or bearish on BTC
3. A regression model is trained based on the day range price deltas and the day's sentiment average 
  Given a score average, a differential to the next day is predicted
4. Live stream of tweets are first fed to the NBmodel, which output a series of sentiment scores
5. The sentiment scores of a given period would then be averaged and fed to the regression model to output a predicted price change.
