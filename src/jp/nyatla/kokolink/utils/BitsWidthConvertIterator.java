package jp.nyatla.kokolink.utils;

import jp.nyatla.kokolink.interfaces.IRecoverableIterator;
import jp.nyatla.kokolink.types.Py__class__.IPyIterator;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.utils.recoverable.RecoverableStopIteration;



// """ 任意ビット幅のintストリームを任意ビット幅のint値に変換するイテレータです。
// """
public class BitsWidthConvertIterator implements IRecoverableIterator<Integer>{
	static public class StopIteration_BitsWidthConvertIterator_FractionalBitsLeft extends PyStopIteration
	{
	    /**
		 * 
		 */
		private static final long serialVersionUID = 615542436956744137L;
		public StopIteration_BitsWidthConvertIterator_FractionalBitsLeft(){
	    	super();
	    }
	    public StopIteration_BitsWidthConvertIterator_FractionalBitsLeft(Exception innerException){
	    	 super(innerException);
	    }

	}

	
	private IPyIterator<Integer> _src;
    private boolean _is_eos;
    private int _input_bits;
    private int _output_bits;
    private int _bits;//private UInt32 _bits;
    private int _n_bits;
    // def __init__(self,src:Iterator[int],input_bits:int=8,output_bits:int=1):
    //     """
    //     """
    //     super().__init__()
    //     self._src=src
    //     self._is_eos=False
    //     self._input_bits=input_bits
    //     self._output_bits=output_bits
    //     self._bits  =0#byte値
    //     self._n_bits=0 #読み出し可能ビット数
    public BitsWidthConvertIterator(IPyIterator<Integer> src) {
    	this(src,8,1);
    }
    public BitsWidthConvertIterator(IPyIterator<Integer> src,int input_bits,int output_bits)
    {
        this._src=src;
        this._is_eos=false;
        this._input_bits=input_bits;
        this._output_bits=output_bits;
        this._bits  =0;//#byte値
        this._n_bits=0;//#読み出し可能ビット数

    }


    public Integer next() throws PyStopIteration
    {
        if(this._is_eos){
            throw new PyStopIteration();
        }
        var n_bits=this._n_bits;
        var bits  =this._bits;
        while(n_bits<this._output_bits){
            int d;//uint d;
            try{
                d=(int)this._src.next();//d=(uint)this._src.next();
            }catch(RecoverableStopIteration e){
                this._bits=bits;
                this._n_bits=n_bits;
                throw e;
            }catch(PyStopIteration e){
                this._is_eos=true;
                if(n_bits!=0){
                    // # print("Fraction")
                    throw new StopIteration_BitsWidthConvertIterator_FractionalBitsLeft(e);
                }
                throw new PyStopIteration(e);
            }
            bits=(bits<<this._input_bits) | d;
            n_bits=n_bits+ this._input_bits;
        }
        int r=0;//uint r=0;
        for(var i=0;i<this._output_bits;i++){
            r=(r<<1) | ((bits>>(n_bits-1))&0x01);
            n_bits=n_bits-1;
        }
        this._n_bits=n_bits;
        this._bits=bits;
        return (int)r;
    }
}
