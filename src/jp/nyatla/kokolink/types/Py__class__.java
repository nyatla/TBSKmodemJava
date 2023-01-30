package jp.nyatla.kokolink.types;

import java.util.Arrays;
import java.util.Iterator;

import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;

public class Py__class__{
    static public class PyStopIteration extends Exception
    {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2528600423615068962L;
		public PyStopIteration() {
        	super();
        }
        public PyStopIteration(Exception innerException) {
        	super("", innerException);
        }
    }

    static public class PyIterator<T> implements IPyIterator<T>
    {

        private Iterator<T> _src;
        public PyIterator(Iterable<T> src)
        {
            this._src = src.iterator();
        }
        @Override
        public T next() throws PyStopIteration
        {
            if (!this._src.hasNext())
            {
                throw new PyStopIteration();
            }
            return this._src.next();
        }

    }


    // IPyIteratorを連結するイテレータ
    static public class IterChain<T> implements IPyIterator<T>
    {
        private IPyIterator<IPyIterator<T>> _src;
        private IPyIterator<T> _current;
        @SafeVarargs
		public IterChain(IPyIterator<T> ... src)
        {
            this._src = new PyIterator<IPyIterator<T>>(Arrays.asList(src));
            this._current = null;
        }
        public T next() throws PyStopIteration
        {
            while (true)
            {
                if (this._current == null)
                {
                    try
                    {
                        this._current = this._src.next();
                    }
                    catch (PyStopIteration e)
                    {   //イテレーション配列の終端なら終了。
                        throw e;
                    }
                }
                try
                {
                    return this._current.next();
                }
                catch (PyStopIteration e)
                {   //値取得で失敗したらイテレーションの差し替え。
                    this._current = null;
                    continue;
                }

            }

        }

    }
    //  定数個の値を返すイテレータ
    static public class Repeater<T> implements IPyIterator<T>
    {
        private T _v;
        private int _count;
        public Repeater(T v,int count)
        {
            this._v = v;
            this._count = count;
        }
        public T next() throws PyStopIteration
        {
            if (this._count==0)
            {
                throw new PyStopIteration();
            }
            this._count--;
            return this._v;
        }
    }

    static abstract public class BasicIterator<T> implements IPyIterator<T>{
    }    
}
