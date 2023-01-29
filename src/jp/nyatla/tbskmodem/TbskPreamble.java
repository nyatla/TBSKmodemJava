package jp.nyatla.tbskmodem;

import jp.nyatla.kokolink.protocol.tbsk.preamble.CoffPreamble;
import jp.nyatla.kokolink.protocol.tbsk.preamble.Preamble;
import jp.nyatla.kokolink.protocol.tbsk.toneblock.TraitTone;

public class TbskPreamble {
    public static Preamble createCoff(TraitTone tone){
    	return createCoff(tone,CoffPreamble.DEFAULT_TH,CoffPreamble.DEFAULT_CYCLE);
    }
    public static Preamble createCoff(TraitTone tone, double threshold,int cycle){
    	return new CoffPreamble(tone,threshold,cycle);
    }
}
