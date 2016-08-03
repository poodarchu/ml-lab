-- This is a file that I made of useful queries
-- Check it out, it may have what you need already
-- Dan

-- Gets the average movie rating for a user
SELECT avg(rating)
FROM rating
WHERE uid = 1815755;

-- Gets all movies a user has rated
SELECT R.mid
FROM rating R
WHERE R.uid = 1815755;

-- Gets all the people who have watched a particular movie
SELECT R.uid
FROM rating R
WHERE R.mid = 17303;





-- Finds all entries pertinant to a memory-based query
-- That is, it finds all movies a person watched, then all
-- related people.
-- WARNING: I WOULD NOT RUN THIS QUERY
-- It takes forever to run, and the results are just too massive.
-- Ex: for the query below, it took 19 mins and had 4500000 results.
-- I only leave it here as a reference.
SELECT *
FROM rating R 
WHERE R.mid IN 
	(SELECT mid 
	 FROM rating R2 
	 WHERE R2.uid = 1815755);

-- Like the last query, but just finds out distinct uids related to
-- the query target.
-- WARNING - THIS ALSO TAKES FOREVER
SELECT DISTINCT R.uid
FROM rating R 
WHERE R.mid IN 
	(SELECT mid 
	 FROM rating R2 
	 WHERE R2.uid = 1815755);


-- This query scans to see if duplicates exist grouping
-- on mid and uid.
-- Doesn't really matter, as there are none
SELECT COUNT(*) AS cnt
FROM rating
WHERE mid > 0 AND mid < 5000
GROUP BY mid, uid
HAVING cnt > 1;
