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
    		this.add(i);
    	}
    }
    // """ 信号強度をv倍します。
    // """
    public TraitTone mul(double v){
        for (var i = 0; i < this.size(); i++)
        {
            this.set(i,this.get(i) * v);
        }
        return this;
    }
}

