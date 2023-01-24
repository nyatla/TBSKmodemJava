package jp.nyatla.kokolink.protocol.tbsk.preamble;

import jp.nyatla.kokolink.interfaces.IRoStream;
import jp.nyatla.kokolink.utils.recoverable.RecoverableException;

public interface Preamble{
    IRoStream<Double> getPreamble();
    Integer waitForSymbol(IRoStream<Double> src) throws RecoverableException;
}