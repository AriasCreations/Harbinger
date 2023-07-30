package dev.zontreck.harbinger.thirdparty.jj2000.j2k.decoder;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.HeaderInfo;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.reader.BitstreamReaderAgent;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.reader.HeaderDecoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceMapper;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.decoder.EntropyDecoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.reader.FileFormatReader;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfileException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.BlkImgDataSrc;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgDataConverter;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.invcomptransf.InvCompTransf;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.dequantizer.Dequantizer;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.roi.ROIDeScaler;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.FacilityManager;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MsgLogger;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.synthesis.InverseWT;

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

public class ImgDecoder {
	/**
	 * The parameter information for this class
	 */
	private static final String[][] pinfo = {
			{ "u" , "[on|off]" , "Prints usage information. If specified all other arguments (except 'v') are ignored" ,
					"off" } ,
			{ "v" , "[on|off]" , "Prints version and copyright information" , "off" } ,
			{ "verbose" , "[on|off]" , "Prints information about the decoded codestream" , "on" } ,
			{
					"pfile" ,
					"<filename>" ,
					"Loads the arguments from the specified file. Arguments that are specified on the "
							+ "command line override the ones from the file.\nThe arguments file is a simple text file "
							+ "with one argument per line of the following form:\n  <argument name>=<argument value>\n"
							+ "If the argument is of boolean type (i.e. its presence turns a feature on), then the"
							+ "'on' value turns it on, while the 'off' value turns it off. The argument name does not "
							+ "include the '-' or '+' character. Long lines can be broken into several lines by "
							+ "terminating them with '\\'. Lines starting with '#' are considered as comments. This "
							+ "option is not recursive: any 'pfile' argument appearing in the file is ignored." , null } ,
			{
					"res" ,
					"<resolution level index>" ,
					"The resolution level at which to reconstruct the image (0 means the "
							+ "lowest available resolution whereas the maximum resolution level corresponds to the "
							+ "original image resolution). If the given index is greater than the number of available "
							+ "resolution levels of the compressed image, the image is reconstructed at its highest "
							+ "resolution (among all tile-components). Note that this option affects only the inverse "
							+ "wavelet transform and not the number of bytes read by the codestream parser: this "
							+ "number of bytes depends only on options '-nbytes' or '-rate'." , null } ,
			{
					"i" ,
					"<filename or url>" ,
					"The file containing the JPEG 2000 compressed data. This can be either a "
							+ "JPEG 2000 codestream or a JP2 file containing a JPEG 2000 codestream. In the latter "
							+ "case the first codestream in the file will be decoded. If an URL is specified (e.g., "
							+ "http://...) the data will be downloaded and cached in memory before decoding. This is "
							+ "intended for easy use in applets, but it is not a very efficient way of decoding "
							+ "network served data." , null } ,
			{
					"o" ,
					"<filename>" ,
					"This is the name of the file to which the decompressed image "
							+ "is written. If no output filename is given, the image is displayed on the screen. "
							+ "Output file format is PGX by default. If the extension"
							+ " is '.pgm' then a PGM file is written as output, however this is "
							+ "only permitted if the component bitdepth does not exceed 8. If "
							+ "the extension is '.ppm' then a PPM file is written, however this "
							+ "is only permitted if there are 3 components and none of them has "
							+ "a bitdepth of more than 8. If there is more than 1 component, "
							+ "suffices '-1', '-2', '-3', ... are added to the file name, just "
							+ "before the extension, except for PPM files where all three "
							+ "components are written to the same file." , null } ,
			{
					"rate" ,
					"<decoding rate in bpp>" ,
					"Specifies the decoding rate in bits per pixel (bpp) where the "
							+ "number of pixels is related to the image's original size (Note:"
							+ " this number is not affected by the '-res' option). If it is equal"
							+ "to -1, the whole codestream is decoded. "
							+ "The codestream is either parsed (default) or truncated depending "
							+ "the command line option '-parsing'. To specify the decoding "
							+ "rate in bytes, use '-nbytes' options instead." , "-1" } ,
			{
					"nbytes" ,
					"<decoding rate in bytes>" ,
					"Specifies the decoding rate in bytes. The codestream is either "
							+ "parsed (default) or truncated depending the command line option '-parsing'. To specify "
							+ "the decoding rate in bits per pixel, use '-rate' options instead." , "-1" } ,
			{
					"parsing" ,
					null ,
					"Enable or not the parsing mode when decoding rate is specified "
							+ "('-nbytes' or '-rate' options). If it is false, the codestream "
							+ "is decoded as if it were truncated to the given rate. If it is "
							+ "true, the decoder creates, truncates and decodes a virtual layer"
							+ " progressive codestream with the same truncation points in each code-block." , "on" } ,
			{
					"ncb_quit" ,
					"<max number of code blocks>" ,
					"Use the ncb and lbody quit conditions. If state information "
							+ "is found for more code blocks than is indicated with this option, the decoder "
							+ "will decode using only information found before that point. "
							+ "Using this otion implies that the 'rate' or 'nbyte' parameter "
							+ "is used to indicate the lbody parameter which is the number of "
							+ "packet body bytes the decoder will decode." , "-1" } ,
			{ "l_quit" , "<max number of layers>" ,
					"Specifies the maximum number of layers to decode for any code-block" , "-1" } ,
			{ "m_quit" , "<max number of bit planes>" ,
					"Specifies the maximum number of bit planes to decode for any code-block" , "-1" } ,
			{
					"poc_quit" ,
					null ,
					"Specifies the whether the decoder should only decode code-blocks "
							+ "included in the first progression order." , "off" } ,
			{ "one_tp" , null , "Specifies whether the decoder should only decode the first tile part of each tile." ,
					"off" } ,
			{ "comp_transf" , null ,
					"Specifies whether the component transform indicated in the codestream should be used." , "on" } ,
			{ "debug" , null , "Print debugging messages when an error is encountered." , "off" } ,
			{
					"cdstr_info" ,
					null ,
					"Display information about the codestream. This information is: "
							+ "\n- Marker segments value in main and tile-part headers,"
							+ "\n- Tile-part length and position within the code-stream." , "off" } ,
			{ "nocolorspace" , null , "Ignore any dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace information in the image." , "off" } ,
			{ "colorspace_debug" , null ,
					"Print debugging messages when an error is encountered in the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace module." , "off" } };

	/**
	 * The parameter list (arguments)
	 */
	protected ParameterList pl;

	/**
	 * Parses the inputstream to analyze the box structure of the JP2 file.
	 */
	protected ColorSpace csMap;

	protected BitstreamReaderAgent breader;
	protected HeaderDecoder hd;
	protected DecoderSpecs decSpec;

	/**
	 * Information contained in the codestream's headers
	 */
	private HeaderInfo hi;
	/**
	 * The exit code of the run method
	 */
	private int exitCode;

	public ImgDecoder ( final ParameterList pl ) {
		this.pl = pl;
	}

	/**
	 * Returns the parameters that are used in this class. It returns a 2D
	 * String array. Each of the 1D arrays is for a different option, and they
	 * have 3 elements. The first element is the option name, the second one is
	 * the synopsis and the third one is a long description of what the
	 * parameter is. The synopsis or description may be 'null', in which case it
	 * is assumed that there is no synopsis or description of the option,
	 * respectively.
	 *
	 * @return the options name, their synopsis and their explanation.
	 */
	public static String[][] getParameterInfo ( ) {
		return ImgDecoder.pinfo;
	}

	/**
	 * Returns all the parameters used in the decoding chain. It calls parameter
	 * from each module and store them in one array (one row per parameter and 4
	 * columns).
	 *
	 * @return All decoding parameters
	 * @see #getParameterInfo
	 */
	public static String[][] getAllParameters ( ) {
		final Vector<String[]> vec = new Vector<String[]> ( );
		int i;

		String[][] str = BitstreamReaderAgent.getParameterInfo ( );
		if ( null != str )
			for ( i = str.length - 1; 0 <= i ; i-- )
				vec.addElement ( str[ i ] );

		str = EntropyDecoder.getParameterInfo ( );
		if ( null != str )
			for ( i = str.length - 1; 0 <= i ; i-- )
				vec.addElement ( str[ i ] );

		str = ROIDeScaler.getParameterInfo ( );
		if ( null != str )
			for ( i = str.length - 1; 0 <= i ; i-- )
				vec.addElement ( str[ i ] );

		str = Dequantizer.getParameterInfo ( );
		if ( null != str )
			for ( i = str.length - 1; 0 <= i ; i-- )
				vec.addElement ( str[ i ] );

		str = InvCompTransf.getParameterInfo ( );
		if ( null != str )
			for ( i = str.length - 1; 0 <= i ; i-- )
				vec.addElement ( str[ i ] );

		str = HeaderDecoder.getParameterInfo ( );
		if ( null != str )
			for ( i = str.length - 1; 0 <= i ; i-- )
				vec.addElement ( str[ i ] );

		str = ColorSpaceMapper.getParameterInfo ( );
		if ( null != str )
			for ( i = str.length - 1; 0 <= i ; i-- )
				vec.addElement ( str[ i ] );

		str = ImgDecoder.getParameterInfo ( );
		if ( null != str )
			for ( i = str.length - 1; 0 <= i ; i-- )
				vec.addElement ( str[ i ] );

		str = new String[ vec.size ( ) ][ 4 ];
		for ( i = str.length - 1; 0 <= i ; i-- )
			str[ i ] = vec.elementAt ( i );

		return str;
	}

	public BlkImgDataSrc decode ( final RandomAccessIO in , final FileFormatReader ff , final boolean verbose ) throws IOException,
			ICCProfileException {
		final EntropyDecoder entdec;
		final ROIDeScaler roids;
		final Dequantizer deq;
		final InverseWT invWT;
		final BlkImgDataSrc color;
		int i;

		// +----------------------------+
		// | Instantiate decoding chain |
		// +----------------------------+

		// **** Header decoder ****
		// Instantiate header decoder and read main header
		this.hi = new HeaderInfo ( );
		try {
			this.hd = new HeaderDecoder ( in , this.pl , this.hi );
		} catch ( final EOFException e ) {
			this.error ( "Codestream too short or bad header, unable to decode." , 2 , e );
			return null;
		}

		final int nCompCod = this.hd.getNumComps ( );
		final int nTiles = this.hi.siz.getNumTiles ( );
		this.decSpec = this.hd.getDecoderSpecs ( );

		// Report information
		if ( verbose ) {
			String info = nCompCod + " component(s) in codestream, " + nTiles + " tile(s)\n";
			info += "Image dimension: ";
			for ( int c = 0 ; c < nCompCod ; c++ ) {
				info += this.hi.siz.getCompImgWidth ( c ) + "x" + this.hi.siz.getCompImgHeight ( c ) + " ";
			}

			if ( 1 != nTiles ) {
				info += "\nNom. Tile dim. (in canvas): " + this.hi.siz.xtsiz + "x" + this.hi.siz.ytsiz;
			}
			FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.INFO , info );
		}
		if ( this.pl.getBooleanParameter ( "cdstr_info" ) ) {
			FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.INFO , "Main header:\n" + this.hi.toStringMainHeader ( ) );
		}

		// Get demixed bitdepths
		final int[] depth = new int[ nCompCod ];
		for ( i = 0; i < nCompCod ; i++ ) {
			depth[ i ] = this.hd.getOriginalBitDepth ( i );
		}

		// **** Bit stream reader ****
		try {
			this.breader = BitstreamReaderAgent
					.createInstance ( in , this.hd , this.pl , this.decSpec , this.pl.getBooleanParameter ( "cdstr_info" ) , this.hi );
		} catch ( final IOException e ) {
			this.error ( "Error while reading bit stream header or parsing packets"
					+ ( ( null != e.getMessage ( ) ) ? ( ":\n" + e.getMessage ( ) ) : "" ) , 4 , e );
			return null;
		} catch ( final IllegalArgumentException e ) {
			this.error ( "Cannot instantiate bit stream reader" + ( ( null != e.getMessage ( ) ) ? ( ":\n" + e.getMessage ( ) ) : "" ) , 2 , e );
			return null;
		}

		// **** Entropy decoder ****
		try {
			entdec = this.hd.createEntropyDecoder ( this.breader , this.pl );
		} catch ( final IllegalArgumentException e ) {
			this.error ( "Cannot instantiate entropy decoder" + ( ( null != e.getMessage ( ) ) ? ( ":\n" + e.getMessage ( ) ) : "" ) , 2 ,
					e
			);
			return null;
		}

		// **** ROI de-scaler ****
		try {
			roids = this.hd.createROIDeScaler ( entdec , this.pl , this.decSpec );
		} catch ( final IllegalArgumentException e ) {
			this.error ( "Cannot instantiate roi de-scaler." + ( ( null != e.getMessage ( ) ) ? ( ":\n" + e.getMessage ( ) ) : "" ) , 2 ,
					e
			);
			return null;
		}

		// **** Dequantizer ****
		try {
			deq = this.hd.createDequantizer ( roids , depth , this.decSpec );
		} catch ( final IllegalArgumentException e ) {
			this.error ( "Cannot instantiate dequantizer" + ( ( null != e.getMessage ( ) ) ? ( ":\n" + e.getMessage ( ) ) : "" ) , 2 , e );
			return null;
		}

		// **** Inverse wavelet transform ***
		try {
			// full page inverse wavelet transform
			invWT = InverseWT.createInstance ( deq , this.decSpec );
		} catch ( final IllegalArgumentException e ) {
			this.error ( "Cannot instantiate inverse wavelet transform"
					+ ( ( null != e.getMessage ( ) ) ? ( ":\n" + e.getMessage ( ) ) : "" ) , 2 , e );
			return null;
		}

		invWT.setImgResLevel ( this.breader.getImgRes ( ) );

		// **** Data converter **** (after inverse transform module)
		final ImgDataConverter converter = new ImgDataConverter ( invWT , 0 );

		// **** Inverse component transformation ****
		final InvCompTransf ictransf = new InvCompTransf ( converter , this.decSpec , depth , this.pl );

		// **** Color space mapping ****
		if ( ff.JP2FFUsed && "off".equals ( pl.getParameter ( "nocolorspace" ) ) ) {
			try {
				this.csMap = new ColorSpace ( in , this.hd , this.pl );
				final BlkImgDataSrc channels = this.hd.createChannelDefinitionMapper ( ictransf , this.csMap );
				final BlkImgDataSrc resampled = this.hd.createResampler ( channels , this.csMap );
				final BlkImgDataSrc palettized = this.hd.createPalettizedColorSpaceMapper ( resampled , this.csMap );
				color = this.hd.createColorSpaceMapper ( palettized , this.csMap );

				if ( this.csMap.debugging ( ) ) {
					FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.ERROR , String.valueOf ( this.csMap ) );
					FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.ERROR , String.valueOf ( channels ) );
					FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.ERROR , String.valueOf ( resampled ) );
					FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.ERROR , String.valueOf ( palettized ) );
					FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.ERROR , String.valueOf ( color ) );
				}
			} catch ( final IllegalArgumentException e ) {
				this.error ( "Could not instantiate ICC profiler" + ( ( null != e.getMessage ( ) ) ? ( ":\n" + e.getMessage ( ) ) : "" ) , 1 , e );
				return null;
			} catch ( final ColorSpaceException e ) {
				this.error ( "error processing jp2 dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace information"
						+ ( ( null != e.getMessage ( ) ) ? ( ": " + e.getMessage ( ) ) : "    " ) , 1 , e );
				return null;
			}
		}
		else { // Skip dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace mapping
			return ictransf;
		}

		// This is the last image in the decoding chain and should be
		// assigned by the last transformation:
		return null != color ? color : ictransf;
	}

	/**
	 * Return the information found in the COM marker segments encountered in
	 * the decoded codestream.
	 */
	public String[] getCOMInfo ( ) {
		if ( null == hi ) { // The codestream has not been read yet
			return null;
		}

		final int nCOMMarkers = this.hi.getNumCOM ( );
		Iterator<HeaderInfo.COM> iterator = hi.com.values ( ).iterator ( );
		final String[] infoCOM = new String[ nCOMMarkers ];
		for ( int i = 0 ; i < nCOMMarkers ; i++ ) {
			infoCOM[ i ] = iterator.next ( ).toString ( );
		}
		return infoCOM;
	}

	/**
	 * Returns the exit code of the class. This is only initialized after the
	 * constructor and when the run method returns.
	 *
	 * @return The exit code of the constructor and the run() method.
	 */
	public int getExitCode ( ) {
		return this.exitCode;
	}

	/**
	 * Prints the warning message 'msg' to standard err, prepending "WARNING" to
	 * it.
	 *
	 * @param msg The error message
	 */
	protected void warning ( final String msg ) {
		FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.WARNING , msg );
	}

	/**
	 * Prints the error message 'msg' to standard err, prepending "ERROR" to it,
	 * and sets the exitCode to 'code'. An exit code different than 0 indicates
	 * that there where problems.
	 *
	 * @param msg  The error message
	 * @param code The exit code to set
	 */
	protected void error ( final String msg , final int code ) {
		this.exitCode = code;
		FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.ERROR , msg );
	}

	/**
	 * Prints the error message 'msg' to standard err, prepending "ERROR" to it,
	 * and sets the exitCode to 'code'. An exit code different than 0 indicates
	 * that there where problems. Either the stacktrace or a "details" message
	 * is output depending on the data of the "debug" parameter.
	 *
	 * @param msg  The error message
	 * @param code The exit code to set
	 * @param ex   The exception associated with the call
	 */
	protected void error ( final String msg , final int code , final Throwable ex ) {
		FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.ERROR , msg );
		if ( "on".equals ( pl.getParameter ( "debug" ) ) ) {
			this.exitCode = code;
			ex.printStackTrace ( );
		}
		else {
			this.error ( "Use '-debug' option for more details" , 2 );
		}
	}
}
