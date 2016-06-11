#-*- coding: utf-8 -*-

from numpy import *
import operator

# 读取数据到矩阵
def file2matrix(filename):
    
    # 打开数据文件，读取每行内容
    fr = open(filename)
    arrayOLines = fr.readlines()
    
    # 初始化矩阵
    numberOfLines = len(arrayOLines)
    returnMat = zeros((numberOfLines,3))
    
    # 初始化类标签向量
    classLabelVector = []
    
    # 循环读取每一行数据
    index = 0
    for line in arrayOLines:
        # 去掉回车符
        line = line.strip()
        # 提取4个数据项
        listFromLine = line.split('\t')
        # 将前三项数据存入矩阵
        returnMat[index,:] = listFromLine[0:3]
        # 将第四项数据存入向量
        classLabelVector.append(int(listFromLine[-1]))
        index += 1
    return returnMat,classLabelVector

# 数据归一化
def autoNorm(dataSet):
    # 读取矩阵中数据项的最大和最小值
    minVals = dataSet.min(0)
    maxVals = dataSet.max(0)
    
    # 获得最大和最小值间差值
    ranges = maxVals - minVals
    
    # 初始化输出结果
    normDataSet = zeros(shape(dataSet))
    
    # 获取矩阵的行数
    m = dataSet.shape[0]
    
    # 矩阵运算：实现归一化公式中的 oldValue - min 一步
    normDataSet = dataSet - tile(minVals, (m,1))
    
    # 矩阵除法：实现归一化公式中的除法
    normDataSet = normDataSet/tile(ranges, (m,1))
    
    # 返回归一化后的数据，数据范围及最小值矩阵
    return normDataSet, ranges, minVals

# kNN算法实现    
def classify0(inX, dataSet, labels, k):
    # 获取样本数据数量
    dataSetSize = dataSet.shape[0]
    
    # 矩阵运算，计算测试数据与每个样本数据对应数据项的差值
    diffMat = tile(inX, (dataSetSize,1)) - dataSet
    
    # sqDistances 上一步骤结果平方和
    sqDiffMat = diffMat**2
    sqDistances = sqDiffMat.sum(axis=1)
    
    # 取平方根，得到距离向量
    distances = sqDistances**0.5
    
    # 按照距离从低到高排序
    sortedDistIndicies = distances.argsort()     
    classCount={}          
    
    # 依次取出最近的样本数据
    for i in range(k):
        # 记录该样本数据所属的类别
        voteIlabel = labels[sortedDistIndicies[i]]
        classCount[voteIlabel] = classCount.get(voteIlabel,0) + 1
    
    # 对类别出现的频次进行排序，从高到低
    sortedClassCount = sorted(classCount.iteritems(), key=operator.itemgetter(1), reverse=True)
    
    # 返回出现频次最高的类别
    return sortedClassCount[0][0]

# 算法测试    
def datingClassTest():
    # 设定测试数据的比例
    hoRatio = 0.10
    
    # 读取数据
    datingDataMat,datingLabels = file2matrix('datingTestSet2.txt')
    
    # 归一化数据
    normMat, ranges, minVals = autoNorm(datingDataMat)
    
    # 数据总行数
    m = normMat.shape[0]
    
    # 测试数据行数
    numTestVecs = int(m*hoRatio)
    
    # 初始化错误率
    errorCount = 0.0
    
    # 循环读取每行测试数据
    for i in range(numTestVecs):
    
        # 对该测试人员进行分类
        classifierResult = classify0(normMat[i,:],normMat[numTestVecs:m,:],datingLabels[numTestVecs:m],3)
        
        # 打印KNN算法分类结果和真实的分类
        print "the classifier came back with: %d, the real answer is: %d" % (classifierResult, datingLabels[i])
        
        # 判断KNN算法结果是否准确
        if (classifierResult != datingLabels[i]): errorCount += 1.0
    
    # 打印错误率
    print "the total error rate is: %f" % (errorCount/float(numTestVecs))

# 执行算法测试
datingClassTest()