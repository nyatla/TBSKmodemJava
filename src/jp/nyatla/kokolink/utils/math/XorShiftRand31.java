package jp.nyatla.kokolink.utils.math;

import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;

// """ https://ja.wikipedia.org/wiki/Xorshift
// """
public class XorShiftRand31 implements IPyIterator<Integer>
{
    private long _seed;
    public XorShiftRand31(int seed){
    	this(seed,0);
    }
    public XorShiftRand31(int seed,int skip)
    {
        this._seed=seed;
        
        for(var i=0;i<skip;i++){
            this.next();
        }
    }
    public Integer next(){
        var y=this._seed;
        y = y ^ (y << 13);
        y = y ^ (y >> 17);
        y = y ^ (y << 5);
        y = y & 0x7fffffff;
        this._seed=y;
        return (int)y;
    }
    // def randRange(self,limit:int):
    //     """ 0<=n<limit-1の値を返します。
    //     """
    //     return next(self) % limit
    public int RandRange(int limit){
        return this.next() %limit;
    }
}


