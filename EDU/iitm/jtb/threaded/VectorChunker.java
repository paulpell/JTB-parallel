package EDU.iitm.jtb.threaded;

import java.util.Enumeration;
import java.util.Vector;

public class VectorChunker {

	// creates simple block chunks from the given vector, and returns them as an array of vectors
	public static Vector[] chunk(Vector v, int chunksNo) {
		int i, currentChunk;
		if (chunksNo > v.size()) chunksNo = v.size();
		if (chunksNo <= 0) chunksNo = 1;

		Vector[] ret = new Vector[chunksNo];
		for (i=0; i<ret.length; i++)
			ret[i] = new Vector();
		
		int chunkSize = v.size() / chunksNo;
		
		// if chunkSize is not a divisor of v.size(), not all the chunks will be of the
		// same size. The few first will be of size chunkSize, and the remaining 
		// of size chunkSize + 1. We have to increment chunkSize after v.size() % chunksNo
		// chunks were distributed
		i = currentChunk = 0;
		for (Enumeration e = v.elements(); e.hasMoreElements(); ++i) {
			if (i == chunkSize) {
				++currentChunk;
				i = 0;
				if (currentChunk == chunksNo - (v.size() % chunksNo))
					++chunkSize;
			}
			ret[currentChunk].add(e.nextElement());
		}
		
		return ret;
	}
}
