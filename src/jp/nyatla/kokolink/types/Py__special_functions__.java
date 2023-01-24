package jp.nyatla.kokolink.types;

import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;

public class Py__special_functions__{
    static public interface Py__getitem__<T>{
        // __getitem__関数は this[int i]にマップします。
        T get(int i);
    }
    static public interface Py__len__{
        // __len__関数は Lengthにマップします。

        int getLength();
    }
    static public interface Py__repr__{
        String get__repr__();
    }
    static public interface Py__str__{
        // __str__関数は ToStringにマップします。
        //string? ToString{get;}
    }
    static public interface Py__next__<T>{
        // __next__関数は Nextにマップします。

        T next() throws PyStopIteration;
    }       
}