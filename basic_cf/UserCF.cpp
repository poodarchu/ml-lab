/**
 * User-Item Matrix:
 *       I1   I2   I3   I4   I5   I6
 *  U1
 *  U2
 *  U3
 *  U4               ...
 *  U5
 *  U6

 求解: 预测U1对I4的评分
 PS: 可以更改 C, R 的值,来更改用户对项目的预测评分。 0 < C < N-1, 0 < R < M-1

 */

#include <iostream>
#include <cmath>

using namespace std;

#define N 6
#define M 6

#define K 2

#define R 0  //确定需要推荐的Item的下标: I_1-4(0, 3)
#define C 3

//SimilarityRate SR
struct SR {
    double similarity;
    int index;
}SimArr[N-1];

bool cmp(const SR s1, const SR s2) {
    return s1.similarity > s2.similarity;
}

double Numerator( int *a, int u, int v) {
    double s1 = 0;

    for (int i = 0; i < M; ++i) {
        if (*(a+v*M+i) == 0 || *(a+u*M+i) == 0)
            continue;
        else
            s1+=*(a+v*M+i) * *(a+u*M+i);
            //cout << "Numerator_i =" << s1 << endl;
    }

    //dump cout << "Numerator: " << s1 << endl;
    return s1;
}

double Denominator( int* a, int u, int v) {
    double s2 = 0, s3 = 0;

    for (int i = 0; i < M; ++i) {
        if (*(a+v*M+i) == 0)
            continue;
        else
            s2+= *(a+v*M+i) * *(a+v*M+i);
    }

    for (int i = 0; i < M; ++i) {
        if (*(a+u*M+i) == 0)
            continue;
        else
            s3+= *(a+u*M+i) * *(a+u*M+i);
    }

    //dump out: cout << "Denominator: << sqrt(s2) * sqrt(s3) << endl;
    return sqrt(s2) * sqrt(s3);
}

double Similarity ( int *a, int u, int v) {
    return Numerator(a, u, v) / Denominator(a, u, v);
}

int main() {
    int *a = new int[N*M];
    a[0]= 4,   a[1]=3,   a[2]=0,   a[3]=0,   a[4]=5,   a[5]=1;
    a[6]= 5,   a[7]=5,   a[8]=4,   a[9]=5,  a[10]=4,  a[11]=0;
    a[12]=4,  a[13]=0,  a[14]=5,  a[15]=3,  a[16]=4,  a[17]=2;
    a[18]=0,  a[19]=3,  a[20]=3,  a[21]=0,  a[22]=4,  a[23]=5;
    a[24]=4,  a[25]=4,  a[26]=0,  a[27]=0,  a[28]=0,  a[29]=4;
    a[30]=1,  a[31]=0,  a[32]=2,  a[33]=4,  a[34]=2,  a[35]=5;

    int no; //记录多少个用户对I4评过分。

    double s = 0, ss = 0;
    for (int i=C, j=0; i<M*N && j<N-1; i+=M) {
        if (*(a+i) != 0 && i/M != R) {
            no++;
            SimArr[j].similarity = Similarity(a, R, i/M);
            SimArr[j].index = i/M;
            ++j;
        }
    }

    sort(SimArr, SimArr+no, cmp);

    for (int k = 0; k < no; ++k) {
        cout << "SimArr[" << SimArr[k].index << "] : " << SimArr[k].similarity << endl;
    }

    for (int l = 0; l < K; ++l) {
        s += SimArr[l].similarity * *(a+SimArr[l].index*M);
        ss += SimArr[l].similarity;
    }

    cout << "Consider the top " << K << " similar users, the Predicted Score for U1 to I4 is : " << s / ss << endl;

    return 0;
}