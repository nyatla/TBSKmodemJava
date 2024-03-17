package jp.nyatla.kokolink.protocol.tbsk.toneblock;

import java.util.ArrayList;
import java.util.List;

import jp.nyatla.kokolink.utils.math.XorShiftRand31;

public class PnTone extends TraitTone{
    /**
	 * 
	 */
	private static final long serialVersionUID = 8218960567645922492L;
	static private List<Double> _constructor_init(int seed, int interval, TraitTone base_tone)
    {
        var tone = base_tone != null ? base_tone : new SinTone(20, 8);
        var pn = new XorShiftRand31(seed, 29);
        var c = 0;
        int f=0;
        var d = new ArrayList<Double>();
        for (var i: tone)
        {
            if (c % interval == 0)
            {
                f = (pn.next() & 0x02) - 1;
            }
            c = c + 1;
            d.add(i * f);
        }
        return d;
    }
    public PnTone(int seed,int interval,TraitTone base_tone) {
    	super(_constructor_init(seed, interval,base_tone));
    }
}