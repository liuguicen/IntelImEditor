#include <iostream>
#include<vector>

#include "edgeBlur.h"
#include "MyUtil.h"

using namespace std;

const int di4[4][2] = {{0,  1},
                       {-1, 0},
                       {0,  -1},
                       {1,  0}};
const int di8[8][2] = {{0,  1},
                       {-1, 1},
                       {-1, 0},
                       {-1, -1},
                       {0,  -1},
                       {1,  -1},
                       {1,  0},
                       {1,  1}};
int w = 100, h = 100;

/**
 * 记录下透明像素的信息
 */
void recordTransparentInfo(Mat &src, uchar **transInfo) {
    uchar *p = src.data;
    int w = src.cols, h = src.rows;
    for (int i = 0; i < h; i++) {
        for (int j = 0; j < w; j++) {
            transInfo[i][j] = p[i * w * 4 + j * 4+3];
        }
    }
}

/**
 * 恢复透明像素的信息
 */
void restoreTransparentInfo(Mat &src, uchar **transInfo) {
    uchar *p = src.data;
    int w = src.cols, h = src.rows;
    for (int i = 0; i < h; i++)
        for (int j = 0; j < w; j++)
            p[i * w * 4 + j*4+3] = transInfo[i][j];
}

uchar **createArray(int w, int h) {
    uchar **p = new uchar *[h];
    for (int i = 0; i < h; i++) {
        p[i] = new uchar[w];
        for (int j = 0; j < w; j++)
            p[i][j] = 0;
    }
    return p;
}

void deleteVisArray(uchar ** &p, int h) {
    for (int i = 0; i < h; i++)
        delete[] p[i];
    delete[] p;
}

/*进行边界模糊的算法，没成功
 */
struct MyPoint {
    int x, y;

    MyPoint(int xx, int yy) {
        x = xx;
        y = yy;
    }

    MyPoint() { }
};

void initVisArray(bool **pBoolean, int w, int h);

bool isDivideMyPoint(int y, int x, int *pic) {
    for (int i = 0; i < 4; i++) {
        int ny = y + di4[i][0], nx = x + di4[i][1];
        if ((nx < 0 || nx >= w || ny < 0 || ny >= h) ||
            pic[ny * w + nx] >> 24 != -1)//相邻8个点中有空白点或者到了边界，一定是分界点
            return true;
    }
    return false;
}

void dfs(MyPoint st, vector<MyPoint> &edgeP, int *pic, bool **vis) {
    for (int i = 0; i < 8; i++) {
        int nx = st.y + di8[i][0], ny = st.x + di8[i][1];
        if ((nx < 0 || nx >= w || ny < 0 || ny >= h) || vis[ny][nx] ||
            pic[ny * w + nx] >> 24 != -1)//是透明或者边界点，不管
            continue;
        else if (isDivideMyPoint(ny, nx, pic))//如果非透明点，再判断是否是边界点，是加入并深度访问
        {
            vis[ny][nx] = true;
            MyPoint p(ny, nx);
            edgeP.push_back(p);
            vis[ny][nx] = true;
            dfs(p, edgeP, pic, vis);
        }
    }
}

vector<MyPoint> searchEdge(bool **vis, int w, int h, int *pic) {
    MyPoint st;
    for (int i = 0; i < h; i++) {
        int j;
        for (j = 0; j < w; j++)
            if (pic[i * w + j] >> 24 == -1) {//找到完全不透明的点
                st.y = i;
                st.x = j;
                break;
            }
        if (j < w)break;
    }
    vector<MyPoint> edgeP;
    edgeP.push_back(st);
    vis[st.y][st.x] = true;
    dfs(st, edgeP, pic, vis);
    return edgeP;
}


void blur(vector<MyPoint> &edgeP, int *pic) {
    const int WIDE = 24;
    int s = 0, e = WIDE, mid_x, mid_y;
    while (e < edgeP.size()) {
        int dx = edgeP[e].x - edgeP[s].x;//x坐标差
        int dy = edgeP[e].y - edgeP[s].y;//y坐标差

        float per_dx = (float) (dx * 1.0 / (WIDE >> 1));//单位坐标差
        float per_dy = (float) (dy * 1.0 / (WIDE >> 1));

        mid_x = (edgeP[s].x + edgeP[e].x) >> 1;
        mid_y = (edgeP[s].y + edgeP[e].y) >> 1;
        int number = 0;
        for (int i = 4; i > 0; i--) {//依次增加4-1个单位
            int new_y, new_x;

            //垂直和水平，的情况都能处理到
            new_y = mid_y + (int) (number * per_dx + 0.5);//y加上s个单位dx
            new_x = mid_x + (int) (number * per_dy + 0.5);
            int swap_x = new_x - (dy >> 2);//要交换的
            int swap_y = new_y - (dx >> 2);
            if (new_x < 0 || new_x >= w || new_y < 0 || new_y >= h)continue;
            if (swap_x < 0 || swap_x >= w || swap_y < 0 || swap_y >= h)continue;
            swap(pic[new_y * w + new_x], pic[swap_y * w + swap_x]);
            pic[new_y * w + new_x] = 0xffff0000;
            pic[swap_y * w + swap_x] = 0xffff0000;
            LOGE("交换了点%d", new_x);
            LOGE("交换了点%d", new_y);

            number += i;
        }
        s++;
        e++;
    }
}

Mat edge_blur(bool **vis, Mat src) {
    /*  h = src.rows;
    w = src.cols;
  int *pic = (int *) src.data;
    initVisArray(vis, w, h);
    vector<MyPoint> edgeP = searchEdge(vis, w, h, pic);
    LOGE("边界点的数量：%d", edgeP.size());
    blur(edgeP, pic);
    deleteVisArray(vis, h);
    edgeP.clear();*/
    return src;
}

void initVisArray(bool **vis, int w, int h) {
    for (int i = 0; i < h; i++)
        for (int j = 0; j < w; j++)
            vis[i][j] = false;
}