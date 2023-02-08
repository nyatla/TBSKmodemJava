package jp.nyatla.kokolink.utils.wavefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.nyatla.kokolink.compatibility.Functions;
import jp.nyatla.kokolink.compatibility.IBinaryReader;
import jp.nyatla.kokolink.compatibility.IBinaryWriter;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
import jp.nyatla.kokolink.utils.FloatConverter;
import jp.nyatla.kokolink.utils.wavefile.riffio.Chunk;
import jp.nyatla.kokolink.utils.wavefile.riffio.WaveFile;

public class PcmData
{
	final private WaveFile _wavfile;
    public static PcmData load(IBinaryReader fp) throws IOException
    {
        return new PcmData(fp);
    }
    public static PcmData load(InputStream src) throws IOException
    {
    	var io=new IBinaryReader() {
			@Override
			public Byte[] readBytes(int size) throws IOException {
				return Functions.fromPrimitiveByteArray(src.readNBytes(size));
			}
		};    	
        return new PcmData(io);
    }

    
    public static void dump(PcmData src, IBinaryWriter dest) throws IOException
    {
        src._wavfile.dump(dest);
    }
    public static void dump(PcmData src, OutputStream dest) throws IOException
    {
    	var io=new IBinaryWriter() {
			@Override
			public int writeBytes(List<Byte> buf) throws IOException {
				dest.write(Functions.toPrimitiveByteArray(buf));
				return buf.size();
			}

			@Override
			public int writeBytes(byte[] buf) throws IOException {
				dest.write(buf);
				return buf.length;
			}};
		PcmData.dump(src,io);
    }


    public PcmData(IBinaryReader fp) throws IOException {
    	this._wavfile=new WaveFile(fp);
    }

    public PcmData(Byte[] src, int sample_bits, int frame_rate) {
        this(src,sample_bits,frame_rate,null);
    }
    public PcmData(Byte[] src, int sample_bits, int frame_rate, List<Chunk> chunks) {
        this._wavfile=new WaveFile(frame_rate, sample_bits/8, 1,src, chunks);
    }
    public PcmData(byte[] src, int sample_bits, int frame_rate) {
    	this(src,sample_bits,frame_rate,null);
    }
    public PcmData(byte[] src, int sample_bits, int frame_rate, List<Chunk> chunks) {
    	this(Functions.fromPrimitiveByteArray(src),sample_bits,frame_rate,chunks);
    }



    public PcmData(IPyIterator<Double> src, int sample_bits, int frame_rate) {
        this(src,sample_bits,frame_rate,null);
    }
    public PcmData(IPyIterator<Double> src, int sample_bits, int frame_rate, List<Chunk> chunks) {
    	this(float2bytes(src,sample_bits),sample_bits,frame_rate,chunks);
    }
    

    // """サンプリングビット数
    // """

    public int getSampleBits()
    {
        return this._wavfile.getFmt().getSamplewidth();
    }
    // @property
    // def frame_rate(self)->int:
    //     """サンプリングのフレームレート
    //     """
    //     return self._frame_rate
    public int getFramerate()
    {
        return this._wavfile.getFmt().getFramerate();
    }




    public int getByteslen()
    {
        return this._wavfile.getData().getSize();
    }


    public List<Double> dataAsFloat()
    {
        assert(this._wavfile.getData()!= null);
        var src = this._wavfile.getData().getData();
        var num_of_sample = src.size();
        assert(num_of_sample % (this.getSampleBits() / 8) == 0);

        var ret=new ArrayList<Double>();
        if (this.getSampleBits() == 8)
        {
            for(var i : src)
            {
                ret.add(FloatConverter.byteToDouble(i));
            }

        }else if (this.getSampleBits() == 16)
        {
            for (var i=0;i<num_of_sample;i+=2)
            {
            	int a=((int)(src.get(i)&0xff) | ((int)(src.get(i+1)&0xff) << 8));
            	short v=(short)(a&0xffff);
                ret.add(FloatConverter.int16ToDouble(v));
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
        return ret;
    }    
    





    static public Byte[] float2bytes(IPyIterator<Double> fdata, int bits)
    {
        var ret = new ArrayList<Byte>();
        if (bits == 8)
        {
        	try {
                while (true) {
                    var d = fdata.next();
                    ret.add((byte)(d * 127 + 128));
                }        		
        	}catch(PyStopIteration e) {
        		//nothing to do
        	}
        	
            return ret.toArray(new Byte[0]);
        }
        else if (bits == 16)
        {
            int r = (int)((Math.pow(2, 16) - 1) / 2); //#Daisukeパッチ
        	try {
	            while(true) {
	                var d = fdata.next();
	                var f = d * r;
	                if (f >= 0)
	                {
	                    int v = (int)(Math.min((double)Short.MAX_VALUE, f));
	                    ret.add((byte)(v & 0xff));
	                    ret.add((byte)((v >> 8) & 0xff));
	                }
	                else
	                {
	                    int v = (int)(0xffff + (int)(Math.max(f, (double)Short.MIN_VALUE)) + 1);
	                    ret.add((byte)(v & 0xff));
	                    ret.add((byte)((v >> 8) & 0xff));
	                }
	            }
        	}catch(PyStopIteration e) {
        		//nothing to do
        	}
            return ret.toArray(new Byte[0]);
        }
        throw new RuntimeException("Invalid bits");
    }
 





}