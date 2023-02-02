package jp.nyatla.kokolink.protocol.tbsk.toneblock;

import java.util.ArrayList;

public class TraitTone extends ArrayList<Double>{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1784727942928505888L;
	public TraitTone(Iterable<Double> d)
    {
    	super();
    	for(var i:d) {
    		assert(i*i<=1);//最大値チェック
    		this.add(i);
    	}
    }
    // """ 信号強度をv倍します。
    // """
    public TraitTone mul(double v){
        for (var i = 0; i < this.size(); i++)
        {
        	var n=this.get(i) * v;
        	assert(n*n<=1);//最大値チェック
            this.set(i,n);
        }
        return this;
    }
}

