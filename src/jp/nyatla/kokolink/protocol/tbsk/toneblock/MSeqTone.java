package jp.nyatla.kokolink.protocol.tbsk.toneblock;

import java.util.ArrayList;
import java.util.List;

import jp.nyatla.kokolink.utils.math.MSequence;

// """ トーン信号を巡回符号でBPSK変調した信号です。
//     2^bits-1*len(base_tone)の長さです。
// """
public class MSeqTone extends TraitTone{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2756328812035355226L;
	private List<Integer> _sequence;
    static private List<Double> _constructor_init(MSequence mseq,TraitTone base_tone)
    {
		assert(base_tone!=null);    	
        var tone = base_tone;
        var a = new ArrayList<Double>();
        for (var i :mseq.genOneCycle())
        {
            for (var j : tone)
            {
                a.add(j * (i * 2 - 1));
            }
        }
        return a;
    }
    public MSeqTone(MSequence mseq, TraitTone base_tone) {
    	super(_constructor_init(mseq, base_tone));
        this._sequence = mseq.genOneCycle();
    };


    public List<Integer> getSequence()
    {
        return this._sequence;
    }
}