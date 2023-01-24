package jp.nyatla.kokolink.utils;


import jp.nyatla.kokolink.types.Py__class__.BasicIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.compatibility.Disposable;

public class recoverable
{
    // """ リカバリー可能なStopIterationです。
    //     イテレータチェーンのスループット調停などで、イテレータが再実行の機会を与えるために使います。
    //     この例外を受け取ったハンドラは、__next__メソッドを実行することで前回失敗した処理を再実行することができます。
    //     再実行を行わない場合はStopIterationと同じ振る舞いをしますが、異なる処理系ではセッションのファイナライズ処理が必要かもしれません。
    // """
    static public class RecoverableStopIteration extends PyStopIteration
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1592481321774532453L;
		public RecoverableStopIteration(){
        	super();
        }
        public RecoverableStopIteration(Exception innerException) {
        	super(innerException);
        }

    }
    static abstract public class BasicRecoverableIterator<T> extends BasicIterator<T>
    {
    }


    // """ リカバリー可能なメソッドが創出する例外です。
    //     送出元のメソッドはrecoverメソッドを呼び出すことで再実行できます。
    //     再実行した関数は、再びRecoverableExceptionをraiseする可能性もあります。

    //     再実行しない場合は、例外ハンドラでclose関数で再試行セッションを閉じてください。
    // """
    static public class RecoverableException extends Exception implements Disposable
    {
		private static final long serialVersionUID = -3262508179587153655L;
		private AsyncMethod<?> _recover_instance;
        public RecoverableException(AsyncMethod<?> recover_instance)
        {
            this._recover_instance=recover_instance;
        }
        public <T extends AsyncMethod<U>,U> T detach()
        {
            if (this._recover_instance == null)
            {
                throw new RuntimeException();

            }
            @SuppressWarnings("unchecked")
			var r = (T)this._recover_instance;
            this._recover_instance = null;
            return r;
        }

        // """ 関数を再試行します。再試行可能な状態で失敗した場合は、自分自信を返します。
        // """
        @Override
        public void dispose(){
            try
            {
                this.Close();
            }
            finally
            {
            }
        }
        public void Close()
        {
            if (this._recover_instance != null)
            {
                this._recover_instance.close();
            }
        }
    }
}
