package jp.nyatla.kokolink.utils.math;


/**
 * [-1,1]の値のRMSを計算します。
 * 値は31ビット固定小数点で計算し、結果は16bitIntです。
 */
public class Rms{
	private int[] _buf;
	private int _ptr;
	private long _sum;
	private static int FP=30;
	public Rms(int length)
	{
		assert(length<(1<<FP));	//31bitまで
		this._buf=new int[length];
		this._ptr=0;
		this._sum=0;
	}
	public Rms update(float d) {
		return this.update((double)d);
	}
	public Rms update(double d) {
		double v=d>1?1:(d<-1?-1:d);//1から-1に正規化
		int iv=(int)Math.round(v*v*(1<<FP));//31bit int
		int[] buf=this._buf;
		this._sum=this._sum+iv-buf[this._ptr]; //Σx^2
		buf[this._ptr]=iv;
		this._ptr=(this._ptr+1)%buf.length;
		return this;
	}
	public double getLastRms() {
		//√(Σx^2/0x7fffffff/n)
		return Math.sqrt((double)this._sum/(1<<FP)/this._buf.length);
	}
}