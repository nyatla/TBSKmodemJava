package jp.nyatla.kokolink.utils.math.corrcoef;

import jp.nyatla.kokolink.interfaces.IRecoverableIterator;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;

/**
 * 自己相関計算機です。
 * @author nyatla
 *
 */
public interface ISelfCorrcoefIterator extends IRecoverableIterator<Double>
{
	/**
	 * 正規化したdouble値の自己相関関数を返す。
	 * @param window
	 * @param src
	 * @param shift
	 * @return
	 */
	public static ISelfCorrcoefIterator createNormalized(int window, IPyIterator<Double> src, int shift) {
		return new SelfCorrcoefIterator2(window,src,shift);
		
	}
}
