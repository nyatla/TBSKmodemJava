package jp.nyatla.kokolink.utils.math.corrcoef;

import java.util.ArrayDeque;
import java.util.Queue;

import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;

// from math import sqrt
// from typing import Union



/**
 * src[:]とsrc[shift:]の相関を返すストリームです。
 * n番目に区間[n,n+window]と[n+shift,n+shift+window]の相関値を返します。
 * 開始からwindow-shift個の要素は0になります。
 * 
 * 31bit固定小数点の64bit演算です。
 */
public class SelfCorrcoefIterator2 implements ISelfCorrcoefIterator
{
    private int[][] xyi;
    private int c;
    private int n;
    private long sumxi;
    private long sumxi2;
    private long sumyi;
    private long sumyi2;
    private long sumxiyi;
    private IPyIterator<Double> _srcx;
    private Queue<Integer> _srcy;



    public SelfCorrcoefIterator2(int window, IPyIterator<Double> src, int shift)
    {

        this.xyi = new int[window][];// Functions.Create2dArray<double?>(window,2,null); //#Xi
        for (var i = 0; i < window; i++)
        {
            this.xyi[i] = null;
        }

        this.c = 0;//#エントリポイント
        this.n = 0;//#有効なデータ数
        this.sumxi = 0;
        this.sumxi2 = 0;
        this.sumyi = 0;
        this.sumyi2 = 0;
        this.sumxiyi = 0;
        this._srcx = src;
        this._srcy = new ArrayDeque<Integer>();
        // """初期化
        // """
        for (var i = 0; i < shift; i++)
        {
            this._srcy.add(0);
        }
        return;
    }
    final static int FP=30;
    @Override
    public Double next() throws PyStopIteration
    {
        var c = this.c;
        var l = this.xyi.length;
        var m = c % l;
        int vxs;
        try
        {
        	double dv=this._srcx.next();
        	assert(-1<=dv && dv<=1);
        	vxs = (int)(dv*(1<<FP));
        }
        catch (RecoverableStopIteration e)
        {
            throw e;
        }


        this._srcy.add(vxs);
        var vys = this._srcy.poll();
        assert(vys!=null);
        long vx=vxs;//キャスト
        long vy=vys;//キャスト

        if (this.xyi[m] == null)
        {
            // #値追加
            this.n += 1 - 0;
            this.sumxi += vx - 0;
            this.sumxi2 += ((vx * vx)>>FP) - 0;//vx**2-0;
            this.sumyi += vy - 0;
            this.sumyi2 += ((vy * vy)>>FP) - 0;// vy**2-0;
            this.sumxiyi +=((vx * vy)>>FP) - 0;
            this.xyi[m] = new int[] { vxs, vys };
        }
        else
        {
            // #値削除
            long lxi = this.xyi[m][0];
            long lyi = this.xyi[m][1];
            this.n += 1 - 1;
            this.sumxi += vx - lxi;
            this.sumxi2 += ((vx * vx)>>FP) - ((lxi * lxi)>>FP);
            this.sumyi += vy - lyi;
            this.sumyi2 += ((vy * vy)>>FP) - ((lyi * lyi)>>FP);
            this.sumxiyi += ((vx * vy)>>FP) - ((lxi * lyi)>>FP);
            this.xyi[m][0] = vxs;
            this.xyi[m][1] = vys;
        }

        this.c += 1;
        assert(this.n > 0);
        // if(this.n==0){
        //     return null;
        // }
        if (this.n == 1)
        {
            return 1.;
        }

        var sumxi_ = ((double)this.sumxi)/(1<<FP);
        var meanx_ = sumxi_ / (this.n);
        var sumxi2_ = ((double)this.sumxi2)/(1<<FP);
        var v = (sumxi2_ + (meanx_ * meanx_) * this.n - 2 * meanx_ * sumxi_);
        if (v < 0)
        {
            v = 0;
        }
        var stdx = Math.sqrt(v / (this.n - 1));

        var sumyi_ = ((double)this.sumyi)/(1<<FP);
        var meany_ =sumyi_ / (this.n);
        var sumyi2_ = ((double)this.sumyi2)/(1<<FP);
        v = (sumyi2_ + (meany_ * meany_) * this.n - 2 * meany_ * sumyi_);
        if (v < 0)
        {
            v = 0;
        }
        var stdy = Math.sqrt(v / (this.n - 1));

        var sumxiyi_=((double)(this.sumxiyi))/(1<<FP);
        v = sumxiyi_ + this.n * meanx_ * meany_ - meany_ * sumxi_ - meanx_ * sumyi_;
        var covxy = v / (this.n - 1);
        var r = stdx * stdy == 0 ? 0 : covxy / (stdx * stdy);
        return r > 1 ? 1f : (r < -1 ? -1 : r);
    }
}

