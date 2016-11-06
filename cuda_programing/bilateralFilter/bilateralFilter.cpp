/**
 * @author: poodarchu
 * @date: 2016-11-06
 *
 * Image bilateral filtering
 * 双边滤波是一种保边缘、非线性平滑滤波器，这种滤波器有 3 个参数：gaussian delta, euclidean delta and iterations
 * 随着 euclidean delta 值增大，大多数纹理信息会被过滤掉，然后轮廓信息则会被保留。当 euclidean delta 接近无穷大时，filter 就变成了一个
 * 普通的高斯滤波器。
 * 随着 gaussian delta 增大，图像细致的纹理会变得越来越模糊。
 * 多次迭代有将一幅图片扁平化的效果，这并不会模糊边缘，而是产生一种卡通画的效果。
 */

#include <math.h>

// CUDA utilities and system includes
#include <cuda_runtime.h>
#include <cuda_gl_interop.h>

#include <helper_cuda.h>   // CUDA device initialization helper functions
#include <helper_cuda_gl.h> //CUDA device + OpenGL initialization functions

// Shared Library Test functions
#include <helper_functions.h>   //CUDA SDK helper functions

int main() {
  
}
