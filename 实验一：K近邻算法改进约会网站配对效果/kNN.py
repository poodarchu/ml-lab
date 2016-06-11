# -*- coding: utf-8 -*-

# python version: python 2.7.11

'''
kNN: k Nearest Neighbors
Input: inX: vector to compare to existing dataset(1xN)
       dataset: size m data set of konwn vectors (NxM)
       labels: dataset labels(1xM vector)
       k: number of neighbors to use for comparision(should be an odd number)

Output: the most popular class label
'''

# @author: Poodar Chu

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

# 输入: 文件名字字符串
# 输出: 驯良样本矩阵和类标签向量
def file2Matrix(fileName):
    fr = open(fileName)
    arrayOLines = fr.readlines()
    numberOfLines = len(arrayOLines)  # 得到文件行数

    # 创建以0填充的矩阵(实际上是一个二维数组)
    returnMat = zeros((numberOfLines, 3))
    classLabelVector = []
    index = 0

    # 循环读取每一行数据
    index = 0
    for line in arrayOLines:
        # 去掉回车符
        line = line.strip()
        # 提取4个数据项
        listFromLine = line.split('\t')
        # 将前三项数据存入矩阵
        returnMat[index, :] = listFromLine[0:3]
        # 将第四项数据存入向量
        classLabelVector.append(int(listFromLine[-1]))
        index += 1
    return returnMat, classLabelVector

# 归一化特征值
def autoNorm(dataSet):
    # 参数0使得函数可以从 列中 选取最小值,而不是选取当前行的最小值
    minVals = dataSet.min(0)
    maxVals = dataSet.max(0)
    ranges = maxVals - minVals
    normDataSet = zeros(shape(dataSet))
    m = dataSet.shape[0]
    normDataSet = dataSet - tile(minVals, (m, 1))
    normDataSet = normDataSet/tile(ranges, (m, 1))
    return  normDataSet, ranges, minVals

# 测试算法的分类效果
'''
测试步骤:
1. 读取数据文件中的样本数据到 特征矩阵 和 标签向量
2. 对数据进行归一化,得到归一化的特征矩阵
3. 执行kNN算法对测试数据进行测试,得到分类结果
4. 与实际的分类结果进行对比,记录分类错误率
5. 打印每个人的分类数据及错误率作为最终结果
'''
def datingClassTest():
    # 设定测试数据的比例
    hoRatio = 0.10

    # 读取数据
    datingDataMat, datingLabels = file2Matrix('data/datingTestSet2.txt')

    # 归一化数据
    normMat, ranges, minVals = autoNorm(datingDataMat)

    # 总行数
    m = normMat.shape[0]

    # 测试数据行数
    numTestVecs = int(m*hoRatio)

    # 初始化错误率
    errorCount = 0.0

    # 循环读取每行测试数据
    for i in range(numTestVecs):
        # 对该测试数据进行分类,该算法没有 "训练算法" 这一步,故直接进行测试。
        classifierResult = classify0(normMat[i, :], normMat[numTestVecs:m, :], datingLabels[numTestVecs:m], 3)

        # print "the classifier came back with: %d, the real answer is: %d" % (classifierResult, datingLabels[i])

        # 计算错误率
        if(classifierResult != datingLabels[i]):
            errorCount += 1.0

    # 打印错误率
    errorRatio = errorCount / float(numTestVecs)
    print "the total error count is: %d. rate is: %f" % (errorCount, errorRatio)

# 约会网站预测函数
def classifyPerson():
    resultList = ['not at all', 'in small does', 'in large does']
    percentTats = float(raw_input("percentage of time spent playing video games?"))
    ffMiles = float(raw_input("frequent flier miles earned per year?"))
    iceCream = float(raw_input("liters of ice cream consumed per year?"))

    datingDataMat, datingLabels = file2Matrix('data/datingTestSet2.txt')
    normMat, ranges, minVals = autoNorm(datingDataMat)
    inArr = array([ffMiles, percentTats, iceCream])
    classifierResult = classify0((inArr-minVals)/ranges, normMat, datingLabels, 3)

    print "You will probably like this person", resultList[classifierResult - 1], '.'

# 执行测试
datingClassTest()



