REGISTER file:/usr/lib/pig/piggybank.jar

SET default_parallel 10;
SET pig.udf.profile true;

-- Load Users
users = LOAD 's3://twitter-project-bucket/users.txt' USING PigStorage('\t');

-- Load Tweets
tweets = LOAD 's3://twitter-project-bucket/ProcessedTweets.txt' USING PigStorage('|');

-- Select only those Fields from Tweets and Users that are needed
users = FOREACH users GENERATE (chararray)$0 as UserId, (chararray)$7 as Location;
tweets = FOREACH tweets GENERATE (chararray)$0 as TweetUserId, (chararray)$1 as OriginalTweet, (chararray)$10 as HashTags;

-- Join Tweets With User By UserId
userTweets = JOIN users BY (UserId), tweets BY (TweetUserId);

-- Filter out all empty HashTags in userTweets join
filteredUserTweets = FILTER userTweets BY (HashTags != '##NOTHING##');

-- Select UserId, Location and HashTags. Also Tokenize (using " ") and Flattenize HashTags
filteredUserTweetsWithHashTag = FOREACH filteredUserTweets GENERATE UserId,Location,FLATTEN(TOKENIZE(HashTags)) as HashTag;

-- Select Only Location, HashTag
tweetsWithLocationAndHashTag = FOREACH filteredUserTweetsWithHashTag GENERATE Location, HashTag;

-- Group the data based on Location and HashTag
groupedTweetsByLocationAndHashTag = GROUP tweetsWithLocationAndHashTag BY (Location, HashTag);

-- Count No. Of Instance Of the group -> Flatten the group
groupedTweetsWithCounts = FOREACH groupedTweetsByLocationAndHashTag GENERATE FLATTEN(group), COUNT(tweetsWithLocationAndHashTag) as HashCount;

-- Group based on the Location
groupedResults = GROUP groupedTweetsWithCounts BY Location;

-- Generate Top 5 Most Tweeted Topics Based on HashCount
topFiveTrendingTopics = FOREACH groupedResults {
        descSortTweetsByHashCount = ORDER groupedTweetsWithCounts BY HashCount DESC;
        top5    = LIMIT descSortTweetsByHashCount 5;
        GENERATE group, FLATTEN(top5);
};

-- Sort the Final Results by Location, NoOfTimesTweeted
results = FOREACH topFiveTrendingTopics GENERATE $0 AS Location, $2 AS HashTags, $3 As NoOfTimesTweeted;

-- Store Results in file
STORE results INTO '$OUTPUT';