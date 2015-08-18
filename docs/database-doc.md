# 友盟微社区数据库文档
友盟微社区内部使用[ActiveAndroid][activeandroid]作为数据引擎，如果对于AvtiveAndroid不了解的同学可以参考[官方文档][doc]、[ActiveAndroid--Android轻量级ORM框架][guide]。

[activeandroid]: https://github.com/pardom/ActiveAndroid
[doc]: https://github.com/pardom/ActiveAndroid/wiki/Getting-started
[guide]: http://www.stormzhang.com/openandroid/android/sqlite/2013/12/20/android-orm-tools-activeandroid/

友盟微社区并没有使用ActiveAndroid的ORM特性，而只运用了它的数据存储功能，如果多个表之间存在关系，我们会建立一个中间类型来维护它们之间的关系，这样做是为了避免实体类型变得混乱。

**微社区的Feed相关数据库表如下:**

|    表名   |    				     	描述                    |
|-----------|----------------------------------------------|
| feeditem | 存储FeedItem实体类的信息，包含feed的id、文字信息、类型、发布时间、地理位置等信息。|
| feed_creator | feed与创建者之间的关系表，有`feed_id`与`user_id`两个字段，分别表示feed的id、该feed创建者的id。 **redundant** |
| feed_like | 存储FeedItem与赞之间的关系，该表中只有两个字段，即`feed_id`与`like_id`，分别表示feed的id以及赞的id。每记录表明了Feed与某个赞的关系，一条feed可以有多个赞。|
| feed_comment | feed与评论之间的关系表，一条feed有0到多条评论，该表维护了feed与评论列表的关系。有`feed_id`与`comment_id`两个字段，分别表示feed的id、该feed的一条评论的id。|
| feed_friends | feed与其中@好友的关系表，一条feed可以@ 0或者多个好友，该表维护了feed与@的好友列表的关系。有`feed_id`与`friend_id`两个字段，分别表示feed的id、该feed @的一条位好友的id。|
| feed_topic | feed与其所包含话题的关系表，一条feed可以有0或者多个话题，该表维护了feed与话题列表的关系。有`feed_id`与`topic_id`两个字段，分别表示feed的id、该feed的一个话题的id。|
| imageitem | 存储feed中图片url的表，含有`feed_id`以及低、中、高三个规格的图片url。|



**微社区的评论相关数据库表如下:**

|    表名   |    				     	描述                    |
|-----------|----------------------------------------------|
| comment | 存储FeedItem中一条评论的实体信息，包含评论的文字、创建时间、所属的feed id等。|
| comment_creator | 评论与创建者之间的关系表，有`comment_id`与`creator_id`两个字段，分别表示评论的id、该评论创建者的id。 **redundant** |
| comment_replyUser | 评论与要回复的用户的关系表，当评论是回复某用户时存储这个条信息。有`comment_id`与`creator_id`两个字段，分别表示评论的id、该评论回复的用户id。 **redundant** |



**微社区的赞(Like)相关数据库表如下:**

|    表名   |    				     	描述                    |
|-----------|----------------------------------------------|
| like | 存储一条赞的信息，包含id与自定义字段。 |
| like_creator | 赞与创建者之间的关系表，有`like_id`与`creator_id`两个字段，分别表示赞的id以及该赞的创建者id。 **redundant** |




**微社区的话题相关数据库表如下:**

|    表名   |    				     	描述                    |
|-----------|----------------------------------------------|
| topic | 存储一个话题的信息，包含话题id、话题名、话题图标url、创建时间、话题描述等信息。 |


**微社区的用户相关数据库表如下:**

|    表名   |    				     	描述                    |
|-----------|----------------------------------------------|
| user | 存储一个用户的信息，包含用户id、用户名、用户头像url、用户年龄、用户权限等信息。 |
| user_fans | 存储用户与粉丝的关系表，含有`user_id`与`fans_id`两个字段，分别表示用户id、关注该用户的粉丝id。 |
| user_follow | 存储用户与该用户关注的人的关系表，含有`user_id`与`follow_id`两个字段，分别表示用户id、该用户关注的人的id。  |
| user_topics | 存储用户与该用户关注的话题的关系表，含有`user_id`与`topic_id`两个字段，分别表示用户id、该用户关注的话题的id。|

