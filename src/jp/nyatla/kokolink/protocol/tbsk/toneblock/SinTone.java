package jp.nyatla.kokolink.protocol.tbsk.toneblock;

import java.util.ArrayList;

// """ Sin波形のトーン信号です。
//     このトーン信号を使用したTBSKはDPSKと同じです。
// """
public class SinTone extends TraitTone{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4237686593350268747L;
	static private Iterable<Double> _constructor_init(int points, int cycle)
    {
        var s = Math.PI * 2 / points * 0.5;
        var d1 = new ArrayList<Double>();
        for (var i = 0; i < points; i++)
        {
            d1.add((double)Math.sin(s + i * Math.PI * 2 / points));
        }
        var d2 = new ArrayList<Double>();
        for (var i = 0; i < cycle; i++)
        {
            d2.addAll(d1);
        }
        return d2;

    }
    public SinTone(int points,int cycle) {
    	super(SinTone._constructor_init(points,cycle));
    }
}
