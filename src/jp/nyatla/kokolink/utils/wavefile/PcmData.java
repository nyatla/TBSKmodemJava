package jp.nyatla.kokolink.utils.wavefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.nyatla.kokolink.compatibility;
import jp.nyatla.kokolink.compatibility.IBinaryReader;
import jp.nyatla.kokolink.compatibility.IBinaryWriter;
import jp.nyatla.kokolink.types.Py__class__.PyStopIteration;
import jp.nyatla.kokolink.types.Py_interface__.IPyIterator;
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
				return compatibility.fromPrimitiveByteArray(src.readNBytes(size));
			}

			@Override
			public int readInt32LE() throws IOException {
				byte[] b=src.readNBytes(4);
				return (int)(((0xff&b[3]) << 24) | ((0xff&b[2]) << 16) | ((0xff&b[1]) << 8) | (0xff & b[0]));
			}

			@Override
			public int readInt16LE() throws IOException {
				byte[] b=src.readNBytes(2);
				return (int)((0xff&b[1]) << 8) | (0xff&b[0]);
			}};    	
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
				dest.write(compatibility.toPrimitiveByteArray(buf));
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
        this._wavfile=new WaveFile(frame_rate, sample_bits/8, 1,compatibility.fromPrimitiveByteArray(src), chunks);
    }



    public PcmData(IPyIterator<Double> src, int sample_bits, int frame_rate) {
        this(src,sample_bits,frame_rate,null);
    }
    public PcmData(IPyIterator<Double> src, int sample_bits, int frame_rate, List<Chunk> chunks) {
        this._wavfile=new WaveFile(
    		frame_rate,
    		sample_bits / 8,
    		1,
    		float2bytes(src,sample_bits),
    		chunks);
    }
    

    // """サンプリングビット数
    // """

    int getSampleBits()
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
        var data = this._wavfile.getData().getData();
        var bits = this.getSampleBits();

        var ret = new ArrayList<Double>();
        if (bits == 8) {
            for (var i = 0;i < data.size();i++) {
                ret.add(((int)(data.get(i)& 0xff)) / 255. - 0.5);
            }
            return ret;
        }
        else if (bits == 16) {
        	int data_size=data.size();
            if (data_size % 2 != 0) {
                data_size = data_size - 1;
            }
            //TBSK_ASSERT(data_size%2==0);
            double r = (Math.pow(2, 16) - 1) / 2;//(2 * *16 - 1)//2 #Daisukeパッチ
            int c = 0;
            int b = 0;
            for (var i :data)
            {
                b = (int)(((b >> 8)&0xff) | (0xff&i) << 8);
                c = (c + 1) % 2;
                if (c == 0) {
                    if ((0x8000 & b) == 0) {
                        ret.add(b / r);
                    }
                    else
                    {
                        ret.add(((b - 0x0000ffff) - 1) / r);
                    }
                    b = 0;
                }
            }
            return ret;
        }
        throw new RuntimeException("invalid bits");
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
    static Byte[] float2bytes(Double[] fdata, int bits) {
    	IPyIterator<Double> a=compatibility.toDoublePyIterator(fdata);
    	
        return float2bytes(a, bits);
    }





}