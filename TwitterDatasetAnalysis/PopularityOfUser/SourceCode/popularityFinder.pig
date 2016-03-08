-- REGISTER file:/usr/local/pig/pig-0.14.0/lib/piggybank.jar;


F = LOAD '$INPUT' USING PigStorage('\t') AS (UserID:chararray, Username:chararray, FriendCount:chararray, FollowerCount:int, StatusCount:chararray, FavCount:chararray, AccountAge:chararray, Location:chararray);

Filtered_F = FILTER F BY UserID != '' AND Username != '' AND FriendCount!='' AND AccountAge!='';

Binned_F = FOREACH Filtered_F generate UserID, Username, (int)FollowerCount, (FollowerCount<23 ? 1 : ((FollowerCount>=23 and FollowerCount<81) ? 2 : ((FollowerCount>=81 and FollowerCount<222) ? 3 : ((FollowerCount>=222 and FollowerCount<663) ? 4 : 5)))) AS (Bin:int);

Grouped_F = GROUP Binned_F BY Bin;

flattened_result = FOREACH Grouped_F GENERATE group, flatten(Binned_F);

STORE flattened_result into '$OUTPUT';

