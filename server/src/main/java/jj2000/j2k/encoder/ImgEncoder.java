package jj2000.j2k.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import jj2000.j2k.codestream.writer.CodestreamWriter;
import jj2000.j2k.codestream.writer.HeaderEncoder;
import jj2000.j2k.codestream.writer.PktEncoder;
import jj2000.j2k.entropy.encoder.EntropyCoder;
import jj2000.j2k.entropy.encoder.PostCompRateAllocator;
import jj2000.j2k.fileformat.writer.FileFormatWriter;
import jj2000.j2k.image.BlkImgDataSrc;
import jj2000.j2k.image.ImgDataConverter;
import jj2000.j2k.image.Tiler;
import jj2000.j2k.image.forwcomptransf.ForwCompTransf;
import jj2000.j2k.quantization.quantizer.Quantizer;
import jj2000.j2k.roi.encoder.ROIScaler;
import jj2000.j2k.util.CodestreamManipulator;
import jj2000.j2k.util.FacilityManager;
import jj2000.j2k.util.MsgLogger;
import jj2000.j2k.util.OSRandomAccessIO;
import jj2000.j2k.util.ParameterList;
import jj2000.j2k.wavelet.analysis.AnWTFilter;
import jj2000.j2k.wavelet.analysis.ForwardWT;

public class ImgEncoder
{
	/** The parameter information for this class */
	private static final String[][] pinfo = {
			{ "debug", null, "Print debugging messages when an error is encountered.", "off" },
			{
					"disable_jp2_extension",
					"[on|off]",
					"JJ2000 automatically adds .jp2 extension when using 'file_format'"
							+ "option. This option disables it when on.", "off" },
			{ "file_format", "[on|off]", "Puts the JPEG 2000 codestream in a JP2 file format wrapper.", "off" },
			{ "pph_tile", "[on|off]", "Packs the packet headers in the tile headers.", "off" },
			{ "pph_main", "[on|off]", "Packs the packet headers in the main header.", "off" },
			{
					"pfile",
					"<filename of arguments file>",
					"Loads the arguments from the specified file. Arguments that are "
							+ "specified on the command line override the ones from the file.\n"
							+ "The arguments file is a simple text file with one argument per "
							+ "line of the following form:\n  <argument name>=<argument value>\n"
							+ "If the argument is of boolean type (i.e. its presence turns a "
							+ "feature on), then the 'on' value turns it on, while the 'off' "
							+ "value turns it off. The argument name does not include the '-' "
							+ "or '+' character. Long lines can be broken into several lines "
							+ "by terminating them with ''. Lines starting with '#' are "
							+ "considered as comments. This option is not recursive: any 'pfile' "
							+ "argument appearing in the file is ignored.", null },
			{
					"tile_parts",
					"<packets per tile-part>",
					"This option specifies the maximum number of packets to have in "
							+ "one tile-part. 0 means include all packets in first tile-part of each tile", "0" },
			{
					"tiles",
					"<nominal tile width> <nominal tile height>",
					"This option specifies the maximum tile dimensions to use. "
							+ "If both dimensions are 0 then no tiling is used.", "0 0" },
			{
					"ref",
					"<x> <y>",
					"Sets the origin of the image in the canvas system. It sets the "
							+ "coordinate of the top-left corner of the image reference grid, "
							+ "with respect to the canvas origin", "0 0" },
			{
					"tref",
					"<x> <y>",
					"Sets the origin of the tile partitioning on the reference grid, "
							+ "with respect to the canvas origin. The value of 'x' ('y') "
							+ "specified can not be larger than the 'x' one specified in the ref option.", "0 0" },
			{
					"rate",
					"<output bitrate in bpp>",
					"This is the output bitrate of the codestream in bits per pixel."
							+ " When equal to -1, no image information (beside quantization "
							+ "effects) is discarded during compression.\n"
							+ "Note: In the case where '-file_format' option is used, the "
							+ "resulting file may have a larger bitrate.", "-1" },
			{
					"lossless",
					"[on|off]",
					"Specifies a lossless compression for the encoder. This options"
							+ " is equivalent to use reversible quantization ('-Qtype reversible')"
							+ " and 5x3 wavelet filters pair ('-Ffilters w5x3'). Note that "
							+ "this option cannot be used with '-rate'. When this option is "
							+ "off, the quantization type and the filters pair is defined by "
							+ "'-Qtype' and '-Ffilters' respectively.", "off" },
			{
					"i",
					"<image file> [,<image file> [,<image file> ... ]]",
					"Mandatory argument. This option specifies the name of the input "
							+ "image files. If several image files are provided, they have to be"
							+ " separated by commas in the command line. Supported formats are "
							+ "PGM (raw), PPM (raw) and PGX, "
							+ "which is a simple extension of the PGM file format for single "
							+ "component data supporting arbitrary bitdepths. If the extension "
							+ "is '.pgm', PGM-raw file format is assumed, if the extension is "
							+ "'.ppm', PPM-raw file format is assumed, otherwise PGX file "
							+ "format is assumed. PGM and PPM files are assumed to be 8 bits "
							+ "deep. A multi-component image can be specified by either "
							+ "specifying several PPM and/or PGX files, or by specifying one PPM file.", null },
			{
					"o",
					"<file name>",
					"Mandatory argument. This option specifies the name of the output "
							+ "file to which the codestream will be written.", null },
			{ "verbose", null, "Prints information about the obtained bit stream.", "on" },
			{ "v", "[on|off]", "Prints version and copyright information.", "off" },
			{ "u", "[on|off]", "Prints usage information. If specified all other arguments (except 'v') are ignored",
					"off" }, };

	/** The default parameter list (arguments) */
	protected ParameterList defpl;

	/** The parameter list (arguments) */
	protected ParameterList pl;

	public ImgEncoder(final ParameterList pl)
	{
		this.pl = pl;
		this.defpl = pl.getDefaultParameterList();
	}

	public int encode(final BlkImgDataSrc imgsrc, final boolean[] imsigned, final int ncomp, final boolean ppminput, final OutputStream output, final boolean useFileFormat, final boolean verbose) throws IOException
	{
		final Tiler imgtiler;
		final ForwCompTransf fctransf;
		final ImgDataConverter converter;
		final EncoderSpecs encSpec;
		final ForwardWT dwt;
		final Quantizer quant;
		final ROIScaler rois;
		final CodestreamWriter bwriter;
		final EntropyCoder ecoder;
		final PostCompRateAllocator ralloc;
		final HeaderEncoder headenc;
		final StreamTokenizer stok;
		StringTokenizer sgtok;
		final int refx;
		final int refy;
		final int trefx;
		final int trefy;
		final int tw;
		final int th;
		final int pktspertp;
		boolean pphTile = false;
		boolean pphMain = false;
		boolean tempSop = false;
		boolean tempEph = false;
		float rate;

		if ("on".equals(pl.getParameter("pph_tile")))
		{
			pphTile = true;

			if ("off".equals(pl.getParameter("Psop")))
			{
				this.pl.put("Psop", "on");
				tempSop = true;
			}
			if ("off".equals(pl.getParameter("Peph")))
			{
				this.pl.put("Peph", "on");
				tempEph = true;
			}
		}

		if ("on".equals(pl.getParameter("pph_main")))
		{
			pphMain = true;

			if ("off".equals(pl.getParameter("Psop")))
			{
				this.pl.put("Psop", "on");
				tempSop = true;
			}
			if ("off".equals(pl.getParameter("Peph")))
			{
				this.pl.put("Peph", "on");
				tempEph = true;
			}
		}

		if (pphTile && pphMain)
		{
			this.error("Can't have packed packet headers in both main and tile headers", 2);
			return -1;
		}

		if (null == pl.getParameter("tiles"))
		{
			this.error("No tiles option specified", 2);
			return -1;
		}

		if (null == pl.getParameter("rate"))
		{
			this.error("Target bitrate not specified", 2);
			return -1;
		}
		try
		{
			rate = this.pl.getFloatParameter("rate");
			if (-1 == rate)
			{
				rate = Float.MAX_VALUE;
			}
		}
		catch (final NumberFormatException e)
		{
			this.error("Invalid value in 'rate' option: " + this.pl.getParameter("rate"), 2, e);
			return -1;
		}
		try
		{
			pktspertp = this.pl.getIntParameter("tile_parts");
			if (0 != pktspertp)
			{
				if ("off".equals(pl.getParameter("Psop")))
				{
					this.pl.put("Psop", "on");
					tempSop = true;
				}
				if ("off".equals(pl.getParameter("Peph")))
				{
					this.pl.put("Peph", "on");
					tempEph = true;
				}
			}
		}
		catch (final NumberFormatException e)
		{
			this.error("Invalid value in 'tile_parts' option: " + this.pl.getParameter("tile_parts"), 2, e);
			return -1;
		}

		// **** Tiler ****
		// get nominal tile dimensions
		stok = new StreamTokenizer(new StringReader(this.pl.getParameter("tiles")));
		stok.eolIsSignificant(false);

		stok.nextToken();
		if (StreamTokenizer.TT_NUMBER != stok.ttype)
		{
			this.error("An error occurred while parsing the tiles option: " + this.pl.getParameter("tiles"), 2);
			return -1;
		}
		tw = (int) stok.nval;
		stok.nextToken();
		if (StreamTokenizer.TT_NUMBER != stok.ttype)
		{
			this.error("An error occurred while parsing the tiles option: " + this.pl.getParameter("tiles"), 2);
			return -1;
		}
		th = (int) stok.nval;

		// Get image reference point
		sgtok = new StringTokenizer(this.pl.getParameter("ref"));
		try
		{
			refx = Integer.parseInt(sgtok.nextToken());
			refy = Integer.parseInt(sgtok.nextToken());
		}
		catch (final NoSuchElementException e)
		{
			throw new IllegalArgumentException("Error while parsing 'ref' option");
		}
		catch (final NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid number type in 'ref' option");
		}
		if (0 > refx || 0 > refy)
		{
			throw new IllegalArgumentException("Invalid value in 'ref' option ");
		}

		// Get tiling reference point
		sgtok = new StringTokenizer(this.pl.getParameter("tref"));
		try
		{
			trefx = Integer.parseInt(sgtok.nextToken());
			trefy = Integer.parseInt(sgtok.nextToken());
		}
		catch (final NoSuchElementException e)
		{
			throw new IllegalArgumentException("Error while parsing 'tref' option");
		}
		catch (final NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid number type in 'tref' option");
		}
		if (0 > trefx || 0 > trefy || trefx > refx || trefy > refy)
		{
			throw new IllegalArgumentException("Invalid value in 'tref' option ");
		}

		// Instantiate tiler
		try
		{
			imgtiler = new Tiler(imgsrc, refx, refy, trefx, trefy, tw, th);
		}
		catch (final IllegalArgumentException e)
		{
			this.error("Could not tile image" + ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}
		final int ntiles = imgtiler.getNumTiles();

		// **** Encoder specifications ****
		encSpec = new EncoderSpecs(ntiles, ncomp, imgsrc, this.pl);

		// **** Component transformation ****
		if (ppminput && null != pl.getParameter("Mct") && "off".equals(pl.getParameter("Mct")))
		{
			FacilityManager
					.getMsgLogger()
					.printmsg(
							MsgLogger.WARNING,
							"Input image is RGB and no color transform has been specified. Compression performance and "
									+ "image quality might be greatly degraded. Use the 'Mct' option to specify a color transform");
		}
		try
		{
			fctransf = new ForwCompTransf(imgtiler, encSpec);
		}
		catch (final IllegalArgumentException e)
		{
			this.error("Could not instantiate forward component transformation"
					+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** ImgDataConverter ****
		converter = new ImgDataConverter(fctransf);

		// **** ForwardWT ****
		try
		{
			dwt = ForwardWT.createInstance(converter, this.pl, encSpec);
		}
		catch (final IllegalArgumentException e)
		{
			this.error("Could not instantiate wavelet transform"
					+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** Quantizer ****
		try
		{
			quant = Quantizer.createInstance(dwt, encSpec);
		}
		catch (final IllegalArgumentException e)
		{
			this.error("Could not instantiate quantizer" + ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** ROIScaler ****
		try
		{
			rois = ROIScaler.createInstance(quant, this.pl, encSpec);
		}
		catch (final IllegalArgumentException e)
		{
			this.error("Could not instantiate ROI scaler" + ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** EntropyCoder ****
		try
		{
			ecoder = EntropyCoder.createInstance(rois, this.pl, encSpec.cblks, encSpec.pss, encSpec.bms, encSpec.mqrs,
					encSpec.rts, encSpec.css, encSpec.sss, encSpec.lcs, encSpec.tts);
		}
		catch (final IllegalArgumentException e)
		{
			this.error("Could not instantiate entropy coder" + ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""),
					2, e);
			return -1;
		}
			
		final OutputStream os;
		ByteArrayOutputStream baos = null;
		if (0 < pktspertp || pphTile || pphMain || useFileFormat)
		{
			baos = new ByteArrayOutputStream();
			os = baos;
		}
		else
		{
			os = output;
		}
		
		// **** CodestreamWriter ****
		try
		{
			// Rely on rate allocator to limit amount of data
			bwriter = new CodestreamWriter(os, Integer.MAX_VALUE);
		}
		catch (final Exception e)
		{
			output.close();
			if (null != baos)
				baos.close();
			this.error("Could not open codestream output" + ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
			return -1;
		}

		// **** Rate allocator ****
		try
		{
			ralloc = PostCompRateAllocator.createInstance(ecoder, this.pl, rate, bwriter, encSpec);
		}
		catch (final IllegalArgumentException e)
		{
			output.close();
			if (null != baos)
				baos.close();
			this.error("Could not instantiate rate allocator" + ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""),
					2, e);
			return -1;
		}

		// **** HeaderEncoder ****
		headenc = new HeaderEncoder(imgsrc, imsigned, dwt, imgtiler, encSpec, rois, ralloc, this.pl);
		ralloc.setHeaderEncoder(headenc);

		// **** Write header to be able to estimate header overhead ****
		headenc.encodeMainHeader();

		// **** Initialize rate allocator, with proper header
		// overhead. This will also encode all the data ****
		ralloc.initialize();

		// **** Write header (final) ****
		headenc.reset();
		headenc.encodeMainHeader();

		// Insert header into the codestream
		bwriter.commitBitstreamHeader(headenc);

		// **** Now do the rate-allocation and write result ****
		ralloc.runAndWrite();

		// **** Done ****
		bwriter.terminate();
		
		int fileLength = bwriter.getLength();
		
		if (0 < pktspertp || pphTile || pphMain || useFileFormat)
		{
			final OSRandomAccessIO io = new OSRandomAccessIO(baos.toByteArray(), Integer.MAX_VALUE);
			baos.close();
			// **** Tile-parts and packed packet headers ****
			if (0 < pktspertp || pphTile || pphMain)
			{
				try
				{
					final CodestreamManipulator cm = new CodestreamManipulator(io, ntiles, pktspertp, pphMain, pphTile, tempSop, tempEph);
					fileLength += cm.doCodestreamManipulation();
					if (0 < pktspertp)
					{
						FacilityManager.getMsgLogger().println(
								"Created tile-parts containing at most " + pktspertp + " packets per tile.", 4, 6);
					}
					if (pphTile)
					{
						FacilityManager.getMsgLogger().println("Moved packet headers to tile headers", 4, 6);
					}
					if (pphMain)
					{
						FacilityManager.getMsgLogger().println("Moved packet headers to main header", 4, 6);
					}
				}
				catch (final IOException e)
				{
					io.close();
					this.error("Error while creating tileparts or packed packet headers"
							+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
					return -1;
				}
			}

			// **** FileformatWriter ****
			if (useFileFormat)
			{
				try
				{
					final int nc = imgsrc.getNumComps();
					final int[] bpc = new int[nc];
					for (int comp = 0; comp < nc; comp++)
					{
						bpc[comp] = imgsrc.getNomRangeBits(comp);
					}
					final FileFormatWriter ffw = new FileFormatWriter(output, imgsrc.getImgHeight(), imgsrc.getImgWidth(), nc, bpc, fileLength);
					fileLength += ffw.length();
				}
				catch (final IOException e)
				{
					io.close();
					throw new Error("Error while writing JP2 file format");
				}
			}
			io.writeTo(output);
			io.close();
		}
		
		// **** Report results ****
		if (verbose)
		{
			// Print target rate info
			if (-1 != rate)
			{
				FacilityManager.getMsgLogger().println(
						"Target bitrate = " + rate + " bpp (i.e. "
								+ (int) (rate * imgsrc.getImgWidth() * imgsrc.getImgHeight() / 8) + " bytes)", 4, 6);
			}
			// Print achieved rate
			FacilityManager.getMsgLogger().println(
					"Achieved bitrate = " + (8.0f * fileLength / (imgsrc.getImgWidth() * imgsrc.getImgHeight()))
							+ " bpp (i.e. " + fileLength + " bytes)", 4, 6);

			// Display ROI information if needed
			if (null != pl.getParameter("Rroi") && !useFileFormat && 0 == pl.getIntParameter("tile_parts"))
			{
				final int roiLen = bwriter.getOffLastROIPkt();
				FacilityManager.getMsgLogger().printmsg(
						MsgLogger.INFO,
						"The Region Of Interest is encoded in the first " + roiLen + " bytes of the codestream (i.e "
								+ (8.0f * roiLen / (imgsrc.getImgWidth() * imgsrc.getImgHeight())) + " bpp)");

			}
			FacilityManager.getMsgLogger().flush();
		}
		return fileLength;
	}

	/**
	 * Returns the parameters that are used in this class and implementing
	 * classes. It returns a 2D String array. Each of the 1D arrays is for a
	 * different option, and they have 4 elements. The first element is the
	 * option name, the second one is the synopsis, the third one is a long
	 * description of what the parameter is and the fourth is its default value.
	 * The synopsis or description may be 'null', in which case it is assumed
	 * that there is no synopsis or description of the option, respectively.
	 * Null may be returned if no options are supported.
	 * 
	 * @return the options name, their synopsis and their explanation, or null
	 *         if no options are supported.
	 */
	public static String[][] getParameterInfo()
	{
		return ImgEncoder.pinfo;
	}

	/**
	 * Returns all the parameters used in the encoding chain. It calls parameter
	 * from each module and store them in one array (one row per parameter and 4
	 * columns).
	 * 
	 * @return All encoding parameters
	 * 
	 * @see #getParameterInfo
	 */
	public static String[][] getAllParameters()
	{
		final Vector<String[]> vec = new Vector<String[]>();

		String[][] str = ImgEncoder.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = ForwCompTransf.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = AnWTFilter.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = ForwardWT.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = Quantizer.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = ROIScaler.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = EntropyCoder.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = HeaderEncoder.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = PostCompRateAllocator.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = PktEncoder.getParameterInfo();
		if (null != str)
			for (int i = str.length - 1; 0 <= i; i--)
				vec.addElement(str[i]);

		str = new String[vec.size()][4];
		for (int i = str.length - 1; 0 <= i; i--)
			str[i] = vec.elementAt(i);

		return str;
	}

	/** The exit code of the run method */
	private int exitCode;

	/**
	 * Returns the exit code of the class. This is only initialized after the
	 * constructor and when the run method returns.
	 * 
	 * @return The exit code of the constructor and the run() method.
	 */
	public int getExitCode()
	{
		return this.exitCode;
	}

	/**
	 * Prints the warning message 'msg' to standard err, prepending "WARNING" to
	 * it.
	 * 
	 * @param msg
	 *            The error message
	 */
	protected void warning(final String msg)
	{
		FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING, msg);
	}

	/**
	 * Prints the error message 'msg' to standard err, prepending "ERROR" to it,
	 * and sets the exitCode to 'code'. An exit code different than 0 indicates
	 * that there where problems.
	 * 
	 * @param msg
	 *            The error message
	 * 
	 * @param code
	 *            The exit code to set
	 */
	protected void error(final String msg, final int code)
	{
		this.exitCode = code;
		FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, msg);
	}

	protected void error(final String msg, final int code, final Throwable e)
	{
		FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, msg);
		if ("on".equals(pl.getParameter("debug")))
		{
			this.exitCode = code;
			e.printStackTrace();
		}
		else
		{
			this.error("Use '-debug' option for more details", 2);
		}
	}
}
