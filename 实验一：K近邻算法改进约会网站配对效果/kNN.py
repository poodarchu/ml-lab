# -*- coding: utf-8 -*-

'''
kNN: k Nearest Neighbors
Input: inX: vector to compare to existing dataset(1xN)
       dataset: size m data set of konwn vectors (NxM)
       labels: dataset labels(1xM vector)
       k: number of neighbors to use for comparision(should be an odd number)

Output: the most popular class label
@author: Poodar Chu
'''
from numpy import *
import operator  # 运算符模块
from os import listdir

def createDataSet():
    group = array([[1.0, 0.8], [1.0, 0.9], [0.3, 0.2], [0, 0.4]])
    labels = ['A', 'A', 'B', 'B']
    return group, labels

'''
kNN pseudocode:
对未知类别属性的数据机中的每一个点依次执行以下操作：
1， 计算计算已知类别数据集中的点与当前带点之间的距离
2， 按照距离依次递增的顺序排序
3， 选取与当前点距离最小的k个点
4， 确定前k个点所在的类别出现的频率
5， 返回前k个点出现频率最高的类别作为当前点的预测分类
'''
def classify0(inX, dataSet, labels, k):
    dsSize = dataSet.shape[0]
    # tile(A, reps): Constract an array by repeating A the number of times given by reps along each axis.
    diffMat = tile(inX, (dsSize, 1)) - dataSet
    sqDiffMat = diffMat**2
    sqDistances = sqDiffMat.sum(axis=1)
    distances = sqDistances**0.5
    # 对数据按照从小到大的顺序排序
    sortedDistIndices = distances.argsort()
    # 将classCount字典分解为元组列表
    classCount = {}
    for i in range(k):
        voteILabel = labels[sortedDistIndices[i]]
        classCount[voteILabel] = classCount.get(voteILabel, 0) + 1
    # 使用itemgetter方法,按照第二个元素的次序对元组进行排序
    sortedClassCount = sorted(classCount.iteritems(), key=operator.itemgetter(1), reverse=True)
    # 返回发生频率最高的元素的标签
    return  sortedClassCount[0][0]


