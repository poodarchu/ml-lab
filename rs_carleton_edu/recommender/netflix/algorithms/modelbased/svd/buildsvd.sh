#!/bin/bash

# Utility script to serialize SVD files for all of the 
# training/test splits in the movielens dataset. The 
# filenames are based on the setup on CMC307-1, but 
# they should be easily modifiable. 
#
# @author sowellb

for file in `ls /recommender/movielens/1indexed`; do
    basename=${file%.*}
    numMovies="1683"
    numUsers="944"
    source="/recommender/movielens/1indexed/$file"
    dest="/recommender/movielens/1indexed/$basename.svd"

    java netflix.algorithms.modelbased.svd.SVDBuilder $numMovies $numUsers $source $dest
done


for file in `ls /recommender/movielens/0indexed`; do
    basename=${file%.*}
    numMovies="1682"
    numUsers="943"
    source="/recommender/movielens/0indexed/$file"
    dest="/recommender/movielens/0indexed/$basename.svd"

    java netflix.algorithms.modelbased.svd.SVDBuilder $numMovies $numUsers $source $dest
done

