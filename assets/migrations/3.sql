
-- 注意,每条sql语句必须独占一行,否则会出错.
CREATE TABLE comment_temp (
	Id	INTEGER PRIMARY KEY AUTOINCREMENT,
	customField	TEXT,
	_id	TEXT UNIQUE,
	createTime	TEXT,
	feedId	TEXT,
	nextPageUrl	TEXT,
	text	TEXT);

INSERT INTO comment_temp SELECT Id,customField,_id,createTime,feedId,nextPageUrl,text FROM comment;
DROP TABLE comment ;
ALTER TABLE comment_temp RENAME TO comment ;

ALTER TABLE user  ADD isFollowed INTEGER ;
ALTER TABLE feeditem  ADD forwardCount INTEGER ;
ALTER TABLE feeditem  ADD isLiked INTEGER ;
ALTER TABLE feeditem  ADD shareLink TEXT ;
ALTER TABLE feeditem  ADD category TEXT ;
