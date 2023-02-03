package jp.nyatla.kokolink.protocol.tbsk.preamble;

import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.utils.recoverable.RecoverableException;

public interface Preamble{
    IRoStream<Double> getPreamble();
    /**
     * ProambleのTick数
     * @return
     */
    public int getNumberOfTicks();
    Integer waitForSymbol(IRoStream<Double> src) throws RecoverableException;
}