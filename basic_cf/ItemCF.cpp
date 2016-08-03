//
// Created by P. Chu on 8/3/16.
//

#include <iostream>
#include <cmath>

using namespace std;

#define K 2

class U_I {
private:
    int *a;
    int no;     //no记录需要计算多少个项目的相似度
    int M;
    int N;
    int index;
    double sim;
    static const int R=2;
    static const int C=3;

public:
    U_I(){ M=5; N=5; no=0; }

    int GetM(){ return M; }
    int GetN(){ return N; }
    int GetR(){ return R; }
    int GetC(){ return C; }

    double Getsim(){ return sim; }
    double Getindex(){ return index; }
    int Getno(){ return no; }

    void Setno(int n){ no=n; };
    void Setsim(double s){ sim=s; }
    void Setindex(int i){ index=i; }

    void Init_a(){ a=new int[GetM()*GetN()]; }    //初始化矩阵指针

    void GetArr();     //输出矩阵
    void InitArr();       //初始化矩阵
    int ColToCompute(int);  //获取需要计算相似度的列

    double SimNumerator(int);
    double SimDenominator(int);
    double Sim(double,double);
    void GetSimIndex(U_I);    //输出  sim：index
    void GetScore(U_I);    //输出最终评分


} arr[10];

void U_I::GetArr()
{
    for(int i=0; i < M*N; ++i)
    {
        cout << '\t' << a[i];
        if((i+1)%5==0 && i!=0)
            cout<<endl;
    }
}

void U_I::InitArr()
{
    a[0]= 0,  a[1]= 4,  a[2]= 5,  a[3]= 2,  a[4]= 5;
    a[5]= 0,  a[6]= 2,  a[7]= 4,  a[8]= 4,  a[9]= 3;
    a[10]=5,  a[11]=3,  a[12]=2,  a[13]=0,  a[14]=5;
    a[15]=2,  a[16]=4,  a[17]=0,  a[18]=1,  a[19]=1;
    a[20]=3,  a[21]=0,  a[22]=3,  a[23]=4,  a[24]=0;
}

int U_I::ColToCompute(int i)   //返回非0的列
{
    if(a[R*M+i] != 0)
        return i;
    else
        return 0;
}

double U_I::SimNumerator(int c)
{
    double s1 = 0;
    for(int i=0, j=0; i<M*N && j<N; i=i+5)
    {
        if(*(a+c+i)==0 || *(a+C+i)==0)
            continue;
        else
        {
            s1+=*(a+c+i)**(a+C+i);
            ++j;
        }
    }
    cout<<"分子："<<s1<<endl;
    return s1;
}

double U_I::SimDenominator(int c)
{
    double s2=0,s3=0;
    for(int i=0,j=0;i<M*N&&j<N;i=i+5)
    {
        if(*(a+c+i)==0||*(a+C+i)==0)
            continue;
        else
        {
            ++j;
            s2+=*(a+c+i)**(a+c+i);
            s3+=*(a+C+i)**(a+C+i);
        }

    }
    cout<<"分母："<<sqrt(s2) * sqrt(s3)<<endl;
    return sqrt(s2) * sqrt(s3);
}

double U_I::Sim(double s1,double s2)
{
    return s1/s2;
}

bool cmp (U_I s1,U_I s2)
{
    return s1.Getsim()>s2.Getsim();
}

void U_I::GetSimIndex(U_I u)
{
    cout<<"排序后的相似性及索引位置如下："<<endl;
    for(int i=0;i<u.Getno();++i)
    {
        cout<<arr[i].sim<<":"<<arr[i].index<<endl;
    }
}


void U_I::GetScore(U_I u)
{

    double s=0,ss=0;
    for(int i=0;i<K;++i)
    {
        s+=arr[i].sim*a[arr[i].index+u.GetR()*u.GetM()];
        ss+=arr[i].sim;

    }
    cout<<"评分为："<<s/ss<<endl;
}
int main()
{
    int c;
    double simi;

    // U_I arr[10];
    U_I u;
    u.Init_a();
    u.InitArr();
    u.GetArr();

    for(int i=0; i<u.GetM(); ++i)
    {
        c = u.ColToCompute(i);
        if(c)
        {
            u.Setno(u.Getno()+1);
            simi = u.Sim(u.SimNumerator(c), u.SimDenominator(c));
            arr[i].Setsim(simi);
            arr[i].Getsim();
            arr[i].Setindex(c);
            arr[i].Getindex();
            // cout << "sim:" << u.sim << " index: " << u.index << endl;
        }

    }

    sort(arr,arr+u.Getno()+1, cmp);
    u.GetSimIndex(u);

    /* dump out:
     *
     * cout<<"排序后的相似性及索引位置如下："<<endl;
     * for(int i=0; i<no; ++i)
     *      cout<<arr[i].Getsim()<<":"<<arr[i].Getindex()<<endl;
     */

    u.GetScore(u);
    return 0;
}