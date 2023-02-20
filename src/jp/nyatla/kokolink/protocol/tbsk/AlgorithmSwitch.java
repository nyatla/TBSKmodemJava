package jp.nyatla.kokolink.protocol.tbsk;

import jp.nyatla.kokolink.utils.math.corrcoef.SelfCorrcoefIterator2;
import jp.nyatla.kokolink.utils.math.corrcoef.ISelfCorrcoefIterator;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;

public class AlgorithmSwitch {
	public static ISelfCorrcoefIterator createSelfCorrcoefIterator(int window, IPyIterator<Double> src)
    {
        return createSelfCorrcoefIterator(window, src,0);
    }
	public static ISelfCorrcoefIterator createSelfCorrcoefIterator(int window, IPyIterator<Double> src, int shift)
    {
        return new SelfCorrcoefIterator2(window, src,shift);
    }
}
