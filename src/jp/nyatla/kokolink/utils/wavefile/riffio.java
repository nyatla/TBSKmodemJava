package jp.nyatla.kokolink.utils.wavefile;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.nyatla.kokolink.compatibility;
import jp.nyatla.kokolink.compatibility.IBinaryReader;
import jp.nyatla.kokolink.compatibility.IBinaryWriter;
import jp.nyatla.kokolink.compatibility.MemBuffer;

public class riffio
{

	
	static public class Chunk
	{
		final protected MemBuffer _buf=new MemBuffer();
		final private int _name;
		final private int _size;
		public Chunk(String name,int size)
		{
			this._name=this._buf.writeBytes(name,4);
			this._size=this._buf.writeInt32LE(size);
		}
		public Chunk(IBinaryReader s) throws IOException{
			this._name=this._buf.writeBytes(s,4);
			this._size=this._buf.writeBytes(s,4);
		}
		public String getName() {
			return this._buf.asStr(this._name,4);
		}
		public int getSize() {
			return this._buf.asInt32LE(this._size);
		}
	    public int dump(IBinaryWriter writer) throws IOException{
	        return this._buf.dump(writer);
	    }
	}
	static public class RawChunk extends Chunk
	{
		final private int _data;
		public RawChunk(String name,Byte[] data){
			super(name,data.length);
			this._data=this._buf.writeBytes(data,data.length%2);
		}
		public RawChunk(String name,int size,IBinaryReader fp) throws IOException{
			super(name,size);
			this._data=this._buf.writeBytes(fp,this.getSize() + this.getSize() % 2);
		}
		
		public List<Byte> getData() {
			return this._buf.subList(this._data,this._buf.size());
		}

	}
	static public class FmtChunk extends Chunk
	{
        private static int CHUNK_SIZE = 2 + 2 + 4 + 4 + 2 + 2;
        private static int WAVE_FORMAT_PCM = 0x0001;		
        public FmtChunk(int size,IBinaryReader fp) throws IOException{
			super("fmt ",size);
	        this._buf.writeBytes(fp, 2);
	        this._buf.writeBytes(fp, 2);
	        this._buf.writeBytes(fp, 4);
	        this._buf.writeBytes(fp, 4);
	        this._buf.writeBytes(fp, 2);
	        this._buf.writeBytes(fp, 2);
		}
        public FmtChunk(int framerate, int samplewidth, int nchannels)
	    {
	    	super("fmt ", FmtChunk.CHUNK_SIZE);
			this._buf.writeInt16LE(WAVE_FORMAT_PCM); //+0
			this._buf.writeInt16LE(nchannels);//+2
			this._buf.writeInt32LE(framerate);//+4
			this._buf.writeInt32LE(nchannels * framerate * samplewidth);//+8
			this._buf.writeInt16LE(nchannels * samplewidth);//+12
			this._buf.writeInt16LE(samplewidth * 8);//+14
	    }
	    public int getNchannels()
	    {
	        return this._buf.asInt16LE(2+8);
	    }
	
	    int getFramerate()
	    {
	        return this._buf.asInt32LE(4+8);
	    }
	
	    int getSamplewidth()
	    {
	        return this._buf.asInt16LE(14+8);
	    }		
	}
	static public class DataChunk extends RawChunk{
		public DataChunk(int size, IBinaryReader fp) throws IOException {
			super("data", size, fp);
		}
		public DataChunk(Byte[] data){
	    	super("data", data);
	    }
	}
	static public class ChunkHeader extends Chunk
	{
		final private int _form;
	    public ChunkHeader(IBinaryReader fp) throws IOException
	    {
	    	super(fp);
	    	this._form=this._buf.writeBytes(fp, 4);
	    }
	    public ChunkHeader(String name, int size, IBinaryReader fp) throws IOException {
	    	super(name, size);
	        this._form=this._buf.writeBytes(fp, 4);
	    }
	    public ChunkHeader(String name, int size,String form) {
	    	 super(name, size);
	         this._form=this._buf.writeBytes(form, 4);
	    }
	    public String getForm()
	    {
	        return this._buf.asStr(this._form,4);
	    }
    }
	
	static public class RiffHeader extends ChunkHeader{
    	public RiffHeader(IBinaryReader fp) throws IOException
    	{
        	super(fp);
            assert(this.getName()=="RIFF");
        }
        public RiffHeader(int size,String  form) {
        	super("RIFF", size, form);
        }
    }
    static public class RawListChunk extends ChunkHeader
    {
    	final private int _payload;
    	final private int _payload_len;
    	public RawListChunk(IBinaryReader fp) throws IOException {
    		super(fp);
            assert(this.getName()=="LIST");
            this._payload_len=this.getSize() - 4;
            this._payload=this._buf.writeBytes(fp,this._payload_len);
    	}
	    public RawListChunk(int size, IBinaryReader fp) throws IOException {
	    	super("LIST", size, fp);
            this._payload_len=this.getSize() - 4;
	        this._payload=this._buf.writeBytes(fp,this._payload_len);
	    }
	    public RawListChunk(String form,Byte[] payload, int payload_len) {
	    	super("LIST", payload_len + 4, form);
            this._payload_len=this.getSize() - 4;
	        this._payload=this._buf.writeBytes(payload);
	    }
	    public byte[] getPayload() {
	        return this._buf.asBytes(this._payload,this._payload_len);
	    }	
    }
    static public class WaveFile extends RiffHeader{
    	final private List<Chunk> _chunks;
    	public WaveFile(IBinaryReader fp) throws IOException
    	{
    		super(fp);
    	    this._chunks=new ArrayList<Chunk>();
    	    assert(this.getForm().compareTo("WAVE")==0);
    	    var chunk_size = this.getSize();
    	    chunk_size -= 4;//fmt分
    	    while (chunk_size > 8)
    	    {    	    	
    	    	String name;
    	    	try {
    	        	name=new String(compatibility.toPrimitiveArray(fp.readBytes(4)));
    	        }
    	        catch (Exception e) {
    	            break;
    	        }
    	        int size=fp.readInt32LE();
    	        chunk_size -= 8+size+(size%2);
    	        if (name.compareTo("fmt ")==0) {
    	            this._chunks.add(new FmtChunk(size, fp));
    	        }
    	        else if (name.compareTo("data")==0) {
    	            this._chunks.add(new DataChunk(size, fp));
    	        }
    	        else if (name.compareTo("LIST")==0){
    	            this._chunks.add(new RawListChunk(size, fp));
    	        }
    	        else {
    	            this._chunks.add(new RawChunk(name, size, fp));
    	        }
    	    }
    	}
    	public static int toSize(int frames_len,List<Chunk> extchunks)
    	{
    	    int s = 4;//form
    	    s = s + FmtChunk.CHUNK_SIZE + 8;
    	    s = s + frames_len + 8;
    	    if (extchunks != null) {
    	        for (int i = 0;i < extchunks.size();i++) {
    	            var cs = extchunks.get(i).getSize();
    	            s = s + cs + cs % 2 + 8;
    	        }
    	    }
    	    return s;
    	}
    	public WaveFile(int samplerate, int samplewidth, int nchannel,Byte[] frames, List<Chunk> extchunks)
    	{
    	    super(toSize(frames.length,extchunks), "WAVE");
    	    this._chunks=new ArrayList<Chunk>();
    	    this._chunks.add(new FmtChunk(samplerate, samplewidth, nchannel));
    	    this._chunks.add(new DataChunk(frames));
    	    if (extchunks != null) {
    	        for (var i = 0;i < extchunks.size();i++) {
    	            this._chunks.add(extchunks.get(i));
    	        }
    	    }
    	}




    	public DataChunk getData()
    	{
    	    var ret=this.getChunk("data");
    	    if (ret == null) {
    	        return null;
    	    }
    	    return (DataChunk)ret;
    	}
    	public FmtChunk getFmt()
    	{
    	    var ret = this.getChunk("fmt ");
    	    if (ret == null) {
    	        return null;
    	    }
    	    return (FmtChunk)ret;

    	}


    	public Chunk getChunk(String name)
    	{
    	    for (var i = 0;i < this._chunks.size();i++) {
    	        if (this._chunks.get(i).getName().compareTo(name)==0) {
    	            return this._chunks.get(i);
    	        }
    	    }
    	    return null;
    	}
    	public int dump(IBinaryWriter writer) throws IOException
    	{
    	    int ret=0;
    	    ret += super.dump(writer);
    	    for (var i = 0;i < this._chunks.size();i++) {
    	        ret += this._chunks.get(i).dump(writer);
    	    }
    	    return ret;
    	}    	
    }

// if __name__ == '__main__':
//     with open("cat1.wav","rb") as f:
//         src=f.read()
//         r=WaveFile(BytesIO(src))
//         print(r)
//         dest=r.toChunkBytes()
//         print(src==dest)
//         for i in range(len(src)):
//             if src[i]!=dest[i]:
//                 print(i)
//         with open("ssss.wav","wb") as g:
//             g.write(dest)
//         n=WaveFile(44100,2,2,r.chunk(b"data").data)
//         with open("ssss2.wav","wb") as g:
//             g.write(n.toChunkBytes())

//         n=WaveFile(44100,2,2,r.chunk(b"data").data,[
//             InfoListChunk([
//                     (b"IARL",b"The location where the subject of the file is archived"),
//                     (b"IART",b"The artist of the original subject of the file"),
//                     (b"ICMS",b"The name of the person or organization that commissioned the original subject of the file"),
//                     (b"ICMT",b"General comments about the file or its subject"),
//                     (b"ICOP",b"Copyright information about the file (e.g., 'Copyright Some Company 2011')"),
//                     (b"ICRD",b"The date the subject of the file was created (creation date)"),
//                     (b"ICRP",b"Whether and how an image was cropped"),
//                     (b"IDIM",b"The dimensions of the original subject of the file"),
//                     (b"IDPI",b"Dots per inch settings used to digitize the file"),
//                     (b"IENG",b"The name of the engineer who worked on the file"),
//                     (b"IGNR",b"The genre of the subject"),
//                     (b"IKEY",b"A list of keywords for the file or its subject"),
//                     (b"ILGT",b"Lightness settings used to digitize the file"),
//                     (b"IMED",b"Medium for the original subject of the file"),
//                     (b"INAM",b"Title of the subject of the file (name)"),
//                     (b"IPLT",b"The number of colors in the color palette used to digitize the file"),
//                     (b"IPRD",b"Name of the title the subject was originally intended for"),
//                     (b"ISBJ",b"Description of the contents of the file (subject)"),
//                     (b"ISFT",b"Name of the software package used to create the file"),
//                     (b"ISRC",b"The name of the person or organization that supplied the original subject of the file"),
//                     (b"ISRF",b"The original form of the material that was digitized (source form)"),
//                     (b"ITCH",b"The name of the technician who digitized the subject file"),]
//                     )])
//         with open("ssss3.wav","wb") as g:
//             g.write(n.toChunkBytes())
//         with open("ssss3.wav","rb") as g:
//             r=WaveFile(g)
//             print(r)

//     with open("ssss2.wav","rb") as f:
//         src=f.read()
//         r=WaveFile(BytesIO(src))
//         print(r)




}