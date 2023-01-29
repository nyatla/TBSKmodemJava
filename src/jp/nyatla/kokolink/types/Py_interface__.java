package jp.nyatla.kokolink.types;

import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;

public class Py_interface__ {
    /*
    IIteratorはPythonのIteratorのエミュレーションインタフェイスです。
    */
    static public interface IPyIterator<T>
    {
        T next() throws PyStopIteration;
    }
}
