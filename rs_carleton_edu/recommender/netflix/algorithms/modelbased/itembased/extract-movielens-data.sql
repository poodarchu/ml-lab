create table top_users (uid int, movie_count int);
insert into top_users select uid, count(*) c from ratings group by uid order by c DESC limit 100;
create index topUserIndex on top_users(uid);

create table top_users_ratings(uid int, mid int, rating int, rating_date date); 
insert into top_users_ratings select uid, mid, rating, rating_date from ratings r where r.uid in (select uid from top_users);
