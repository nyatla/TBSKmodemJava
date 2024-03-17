package jp.nyatla.kokolink.protocol.tbsk.toneblock;

import java.util.ArrayList;
import java.util.List;


public class TraitTone extends ArrayList<Double>{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1784727942928505888L;
	/**
	 * この関数はイテレータを消費します
	 * @param d
	 */
	protected TraitTone(List<Double> d)
    {
    	super();
    	for(var w:d) {
    		assert(w*w<=1);//最大値チェック
    		this.add(w);    		
    	}
    }
	public TraitTone(Double[] d) {
    	super();
    	for(var w:d) {
    		assert(w*w<=1);//最大値チェック
    		this.add(w);    		
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

