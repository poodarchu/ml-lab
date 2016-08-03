/* Recommender database creation script */
/* run using 
	mysql -u recommender -p < create-schema.sql > create-schema-output.txt */
use recommender;
DROP TABLE IF EXISTS rating;
DROP TABLE IF EXISTS movie;

CREATE TABLE movie (
	mid int PRIMARY KEY,
	release_date year,
	title varchar(255)
	);

CREATE TABLE rating (
	uid 	int,
	mid 	int,
	rating int,
	rating_date date,
	PRIMARY KEY (uid, mid, rating_date),
	FOREIGN KEY (mid) REFERENCES movie (mid)
	);

-- Dan's additions
CREATE TABLE averages (
	uid	int,
	avgrating	double,
	PRIMARY KEY (uid),
	FOREIGN KEY (uid) REFERENCES rating (uid)
	);

-- Adds values to averages
INSERT INTO averages SELECT uid, AVG(rating) FROM rating GROUP BY uid;

-- Probe table creation and loading of data
CREATE TABLE probe (
	mid int,
	uid int,
	PRIMARY KEY (mid, uid),
	FOREIGN KEY (mid) REFERENCES movie (mid)
	);

LOAD DATA LOCAL INFILE 'probe2.txt' INTO TABLE probe FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n'
