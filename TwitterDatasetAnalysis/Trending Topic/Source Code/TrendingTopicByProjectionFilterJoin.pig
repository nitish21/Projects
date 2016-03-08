REGISTER file:/usr/lib/pig/piggybank.jar

SET default_parallel 10;
SET pig.udf.profile true;

-- Load Users
users = LOAD 's3://twitter-project-bucket/users.txt' USING PigStorage('\t') as
(UserId:chararray,
 UserName:chararray,
 FriendCount:chararray,
 FollowerCount:chararray,
 StatusCount:chararray,
 FavoriteCount:chararray,
 AccountAge:chararray,
 Location:chararray);

-- Load Tweets
tweets = LOAD 's3://twitter-project-bucket/ProcessedTweets.txt' USING PigStorage('|') as
(TweetUserId:chararray,
 Origin:chararray,
 Text:chararray,
 Url:chararray,
 TweetId:chararray,
 Date:chararray,
 RetweetCount:chararray,
 FavoriteCount:chararray,
 MentionedEntities:chararray,
 HashTags:chararray);

-- Projection : Select UserId, Location from users
usersWithLocation = FOREACH users GENERATE UserId, Location;

-- Projection : Select TweetUserId, HashTags from tweets
tweetsWithHashTags = FOREACH tweets GENERATE TweetUserId, HashTags;

-- Select UserId, HashTags. Also Tokenize (using " ") and Flattenize HashTags
filteredUserTweetsWithHashTag = FOREACH tweetsWithHashTags GENERATE TweetUserId, FLATTEN(TOKENIZE(HashTags)) as HashTag;

-- Filter out all empty HashTags in filteredUserTweetsWithHashTag
filteredTweets = FILTER filteredUserTweetsWithHashTag BY (HashTag != '##NOTHING##');

-- Join Tweets With User By UserId
userTweets = JOIN users BY (UserId), filteredTweets BY (TweetUserId);

-- Select Only Location, HashTag
tweetsWithLocationAndHashTag = FOREACH userTweets GENERATE Location, HashTag;

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