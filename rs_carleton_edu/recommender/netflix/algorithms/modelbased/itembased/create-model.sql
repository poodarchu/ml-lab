USE recommender;

/* Similarity table for item-based collaboration */
DROP TABLE IF EXISTS item_similarity;

CREATE TABLE item_similarity (
	mid1 int,
	mid2 int,
	similarity double);
/*	primary key (mid1, mid2),
	FOREIGN KEY (mid1) REFERENCES movies (mid),
	FOREIGN KEY (mid2) REFERENCES movies (mid)
	);*/

