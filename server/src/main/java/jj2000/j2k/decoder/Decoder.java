/*
 * CVS identifier:
 *
 * $Id: Decoder.java,v 1.74 2002/08/08 14:09:35 grosbois Exp $
 *
 * Class:                   Decoder
 *
 * Description:             The decoder object
 *
 *
 *
 * COPYRIGHT:
 * 
 * This software module was originally developed by Rapha�l Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askel�f (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, F�lix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 * 
 * Copyright (c) 1999/2000 JJ2000 Partners.
 */
package jj2000.j2k.decoder;

import jj2000.j2k.quantization.dequantizer.*;
import jj2000.j2k.image.invcomptransf.*;
import jj2000.j2k.fileformat.reader.*;
import jj2000.j2k.codestream.reader.*;
import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.entropy.decoder.*;
import jj2000.j2k.image.output.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.roi.*;
import jj2000.j2k.io.*;
import jj2000.disp.*;
import jj2000.j2k.*;

import colorspace.*;

import java.awt.image.*;
import java.awt.*;
import java.net.*;
import java.io.*;

/**
 * This class is the main class of JJ2000's decoder. It instantiates all objects
 * and performs the decoding operations. It then writes the image to the output
 * file or displays it.
 * 
 * <p>
 * First the decoder should be initialized with a ParameterList object given
 * through the constructor. The when the run() method is invoked and the decoder
 * executes. The exit code of the class can be obtained with the getExitCode()
 * method, after the constructor and after the run method. A non-zero value
 * indicates that an error has occurred.
 * 
 * <p>
 * The decoding chain corresponds to the following sequence of modules:
 * 
 * <ul>
 * <li>BitstreamReaderAgent</li>
 * <li>EntropyDecoder</li>
 * <li>ROIDeScaler</li>
 * <li>Dequantizer</li>
 * <li>InverseWT</li>
 * <li>ImgDataConverter</li>
 * <li>EnumratedColorSpaceMapper, SyccColorSpaceMapper or ICCProfiler</li>
 * <li>ComponentDemixer (if needed)</li>
 * <li>ImgDataAdapter (if ComponentDemixer is needed)</li>
 * <li>ImgWriter</li>
 * <li>BlkImgDataSrcImageProducer</li>
 * </ul>
 * 
 * <p>
 * The 2 last modules cannot be used at the same time and corresponds
 * respectively to the writing of decoded image into a file or the graphical
 * display of this same image.
 * 
 * <p>
 * The behaviour of each module may be modified according to the current
 * tile-component. All the specifications are kept in modules extending
 * ModuleSpec and accessible through an instance of DecoderSpecs class.
 * 
 * @see BitstreamReaderAgent
 * @see EntropyDecoder
 * @see ROIDeScaler
 * @see Dequantizer
 * @see InverseWT
 * @see ImgDataConverter
 * @see InvCompTransf
 * @see ImgWriter
 * @see BlkImgDataSrcImageProducer
 * @see ModuleSpec
 * @see DecoderSpecs
 */
public class Decoder extends ImgDecoder implements Runnable
{
	/**
	 * Reference to the TitleUpdater instance. Only used when decoded image is
	 * displayed
	 */
	TitleUpdater title;

	/**
	 * False if the Decoder instance is self-contained process, false if thrown
	 * by another process (i.e by a GUI)
	 */
	private boolean isChildProcess;

	/** The default parameter list (arguments) */
	private final ParameterList defpl;

	/** The valid list of options prefixes */
	private static final char[] vprfxs = { BitstreamReaderAgent.OPT_PREFIX, EntropyDecoder.OPT_PREFIX,
			ROIDeScaler.OPT_PREFIX, Dequantizer.OPT_PREFIX, InvCompTransf.OPT_PREFIX, HeaderDecoder.OPT_PREFIX,
			ColorSpaceMapper.OPT_PREFIX };

	/** Frame used to display decoded image */
	private Frame win;

	/** The component where the image is to be displayed */
	private ImgScrollPane isp;

	/**
	 * Instantiates a decoder object, with the ParameterList object given as
	 * argument and a component where to display the image if no output file is
	 * specified. It also retrieves the default ParameterList.
	 * 
	 * @param pl
	 *            The ParameterList for this decoder (contains also defaults
	 *            values).
	 * 
	 * @param isp
	 *            The component where the image is to be displayed if not output
	 *            file is specified. If null a new frame will be created to
	 *            display the image.
	 */
	public Decoder(final ParameterList pl, final ImgScrollPane isp)
	{
		super(pl);
		this.defpl = pl.getDefaultParameterList();
		this.isp = isp;
	}

	/**
	 * Instantiates a decoder object, with the ParameterList object given as
	 * argument. It also retrieves the default ParameterList.
	 * 
	 * @param pl
	 *            The ParameterList for this decoder (contains also defaults
	 *            values).
	 */
	public Decoder(final ParameterList pl)
	{
		this(pl, null);
	}

	/**
	 * Runs the decoder. After completion the exit code is set, a non-zero value
	 * indicates that an error occurred.
	 * 
	 * @see #getExitCode
	 */
	@Override
	public void run()
	{
		final boolean verbose;
		int i;
		final String infile;
		final RandomAccessIO in;
		String outfile = "", outbase = "", outext = "";
		String[] out = null;
		ImgWriter[] imwriter = null;
		boolean disp = false;
		Image img = null;
		final Dimension winDim;
		final Dimension scrnDim;
		Insets ins = null;
		String btitle = "";

		try
		{

			// **** Usage and version ****
			try
			{
				// Do we print version information?
				if (this.pl.getBooleanParameter("v"))
				{
					this.printVersionAndCopyright();
				}
				// Do we print usage information?
				if ("on".equals(pl.getParameter("u")))
				{
					this.printUsage();
					return; // When printing usage => exit
				}
				// Do we print info ?
				verbose = this.pl.getBooleanParameter("verbose");
			}
			catch (final StringFormatException e)
			{
				this.error("An error occurred while parsing the arguments:\n" + e.getMessage(), 1, e);
				return;
			}
			catch (final NumberFormatException e)
			{
				this.error("An error occurred while parsing the arguments:\n" + e.getMessage(), 1, e);
				return;
			}

			// **** Check parameters ****
			try
			{
				this.pl.checkList(Decoder.vprfxs, ParameterList.toNameArray(ImgDecoder.getParameterInfo()));
			}
			catch (final IllegalArgumentException e)
			{
				this.error(e.getMessage(), 2, e);
				return;
			}

			// Get input file
			infile = this.pl.getParameter("i");
			if (null == infile)
			{
				this.error("Input file ('-i' option) has not been specified", 1);
				return;
			}

			// Get output files
			outfile = this.pl.getParameter("o");
			if (null == outfile)
			{
				disp = true;
			}
			else if (-1 != outfile.lastIndexOf('.'))
			{
				outext = outfile.substring(outfile.lastIndexOf('.'));
				outbase = outfile.substring(0, outfile.lastIndexOf('.'));
			}
			else
			{
				outbase = outfile;
				outext = ".pgx";
			}

			// **** Open input files ****
			// Creates a BEBufferedRandomAccessFile of a ISRandomAccessIO
			// instance for reading the file format and codestream data
			if (1 <= infile.indexOf("/") && ':' == infile.charAt(infile.indexOf("/") - 1))
			{ // an URL
				final URL inurl;
				final URLConnection conn;
				final int datalen;
				final InputStream is;

				try
				{
					inurl = new URL(infile);
				}
				catch (final MalformedURLException e)
				{
					this.error("Malformed URL for input file " + infile, 4, e);
					return;
				}
				try
				{
					conn = inurl.openConnection();
					conn.connect();
				}
				catch (final IOException e)
				{
					this.error("Cannot open connection to " + infile
							+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 4, e);
					return;
				}
				datalen = conn.getContentLength();
				try
				{
					is = conn.getInputStream();
				}
				catch (final IOException e)
				{
					this.error("Cannot get data from connection to " + infile
							+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 4, e);
					return;
				}
				if (-1 != datalen)
				{ // known length => initialize to length
					in = new ISRandomAccessIO(is, datalen, 1, datalen);
				}
				else
				{ // unknown length => use defaults
					in = new ISRandomAccessIO(is);
				}
				// HACK: to verify if the URL is valid try to read some data
				try
				{
					in.read();
					in.seek(0);
				}
				catch (final IOException e)
				{
					this.error("Cannot get input data from " + infile + " Invalid URL?", 4, e);
					return;
				}
			}
			else
			{ // a normal file
				try
				{
					in = new BEBufferedRandomAccessFile(infile, "r");
				}
				catch (final IOException e)
				{
					this.error("Cannot open input file " + ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 4, e);
					return;
				}
			}

			// **** File Format ****
			// If the codestream is wrapped in the jp2 fileformat, Read the
			// file format wrapper
			final FileFormatReader ff = new FileFormatReader(in);
			ff.readFileFormat();
			if (ff.JP2FFUsed)
			{
				in.seek(ff.getFirstCodeStreamPos());
			}

			final BlkImgDataSrc decodedImage = this.decode(in, ff, verbose);
			final int nCompImg = decodedImage.getNumComps();

			// **** Create image writers/image display ****
			if (disp)
			{
				// No output file has been specified. Display decoded image

				// Set up the display elements

				btitle = "JJ2000: " + (new File(infile)).getName() + " " + decodedImage.getImgWidth() + "x"
						+ decodedImage.getImgHeight();
				if (null == isp)
				{
					this.win = new Frame(btitle + " @ (0,0) : 1");
					this.win.setBackground(Color.white);
					this.win.addWindowListener(new ExitHandler(this));
					this.isp = new ImgScrollPane(ImgScrollPane.SCROLLBARS_AS_NEEDED);
					this.win.add(this.isp, BorderLayout.CENTER);
					this.isp.addKeyListener(new ImgKeyListener(this.isp, this));
					// HACK to make it work under Windows: for some reason
					// under JDK 1.1.x the event is delivered to the window
					// and not to the ImgScrollPane.
					this.win.addKeyListener(new ImgKeyListener(this.isp, this));
					// END HACK
				}
				else
				{
					this.win = null;
				}

				// Get the window dimension to use, do not use more
				// than 8/10 of the screen size, in either dimension.
				if (null != win)
				{
					this.win.addNotify(); // Instantiate peer to get insets
					ins = this.win.getInsets();
					final int subX = decodedImage.getCompSubsX(0);
					final int subY = decodedImage.getCompSubsY(0);
					final int w = (decodedImage.getImgWidth() + subX - 1) / subX;
					final int h = (decodedImage.getImgHeight() + subY - 1) / subY;
					winDim = new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom);
					scrnDim = this.win.getToolkit().getScreenSize();
					if (winDim.width > scrnDim.width * 8 / 10.0f)
					{
						// Width too large for screen
						winDim.width = (int) (scrnDim.width * 8 / 10.0f);
					}
					if (winDim.height > scrnDim.height * 8 / 10.0f)
					{
						// Height too large for screen
						winDim.height = (int) (scrnDim.height * 8 / 10.0f);
					}
					this.win.setSize(winDim);
					this.win.validate();
					this.win.setVisible(true);
					// Start the title updater
					final Thread tu;
					this.title = new TitleUpdater(this.isp, this.win, btitle);
					tu = new Thread(this.title);
					tu.start();
				}
				else
				{
					this.title = null;
				}
			}
			else
			{ // Write decoded image to specified output file

				// Create output file names
				if (null != csMap)
				{
					if (".PPM".equalsIgnoreCase(outext)
							&& (3 != nCompImg || 8 < decodedImage.getNomRangeBits(0)
									|| 8 < decodedImage.getNomRangeBits(1) || 8 < decodedImage.getNomRangeBits(2)
									|| this.csMap.isOutputSigned(0) || this.csMap.isOutputSigned(1) || this.csMap.isOutputSigned(2)))
					{
						this.error("Specified PPM output file but compressed image is not of the correct format "
								+ "for PPM or limited decoded components to less than 3.", 1);
						return;
					}
				}
				else
				{
					if (".PPM".equalsIgnoreCase(outext)
							&& (3 != nCompImg || 8 < decodedImage.getNomRangeBits(0)
									|| 8 < decodedImage.getNomRangeBits(1) || 8 < decodedImage.getNomRangeBits(2)
									|| this.hd.isOriginalSigned(0) || this.hd.isOriginalSigned(1) || this.hd.isOriginalSigned(2)))
					{
						this.error("Specified PPM output file but compressed image is not of the correct format "
								+ "for PPM or limited decoded components to less than 3.", 1);
						return;
					}
				}
				out = new String[nCompImg];
				// initiate all strings to keep compiler happy
				for (i = 0; i < nCompImg; i++)
				{
					out[i] = "";
				}
				if (1 < nCompImg && !".PPM".equalsIgnoreCase(outext))
				{ // Multiple file output
					// files
					// If PGM verify bitdepth and if signed
					if (".PGM".equalsIgnoreCase(outext))
					{
						for (i = 0; i < nCompImg; i++)
						{
							if (null != csMap)
							{
								if (this.csMap.isOutputSigned(i))
								{
									this.error("Specified PGM output file but compressed image is not of the "
											+ "correct format for PGM.", 1);
									return;
								}
							}
							else
							{
								if (this.hd.isOriginalSigned(i))
								{
									this.error("Specified PGM output file but compressed image is not of the "
											+ "correct format for PGM.", 1);
									return;
								}
							}
						}
					}
					// Open multiple output files
					for (i = 0; i < nCompImg; i++)
					{
						out[i] = outbase + "-" + (i + 1) + outext;
					}
				}
				else
				{ // Single output file
					out[0] = outbase + outext;
				}
				// Now get the image writers
				if (".PPM".equalsIgnoreCase(outext))
				{
					imwriter = new ImgWriter[1];
					try
					{
						imwriter[0] = new ImgWriterPPM(out[0], decodedImage, 0, 1, 2);
					}
					catch (final IOException e)
					{
						this.error("Cannot write PPM header or open output file" + i
								+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
						return;
					}
				}
				else
				{ // PGX or PGM
					imwriter = new ImgWriter[nCompImg];
				}

				// If 3 (originally) unsigned components with depths less than
				// 8 bits and a component transformation is used in at least
				// one tile, ImgWriterPPM is better than ImgWriterPGM (the
				// image is entirely decoded 3 times).
				if (null != csMap)
				{
					if (3 == imwriter.length && 8 >= decodedImage.getNomRangeBits(0)
							&& 8 >= decodedImage.getNomRangeBits(1) && 8 >= decodedImage.getNomRangeBits(2)
							&& !this.csMap.isOutputSigned(0) && !this.csMap.isOutputSigned(1) && !this.csMap.isOutputSigned(2)
							&& this.decSpec.cts.isCompTransfUsed())
					{
						this.warning("JJ2000 is quicker with one PPM output "
								+ "file than with 3 PGM/PGX output files when a"
								+ " component transformation is applied.");
					}
				}
				else
				{
					if (3 == imwriter.length && 8 >= decodedImage.getNomRangeBits(0)
							&& 8 >= decodedImage.getNomRangeBits(1) && 8 >= decodedImage.getNomRangeBits(2)
							&& !this.hd.isOriginalSigned(0) && !this.hd.isOriginalSigned(1) && !this.hd.isOriginalSigned(2)
							&& this.decSpec.cts.isCompTransfUsed())
					{
						this.warning("JJ2000 is quicker with one PPM output "
								+ "file than with 3 PGM/PGX output files when a"
								+ " component transformation is applied.");
					}
				}
			}

			// **** Report info ****
			final int mrl = this.decSpec.dls.getMin();
			if (verbose)
			{
				final int res = this.breader.getImgRes();
				if (mrl != res)
				{
					FacilityManager.getMsgLogger().println(
							"Reconstructing resolution " + res + " on " + mrl + " (" + this.breader.getImgWidth(res) + "x"
									+ this.breader.getImgHeight(res) + ")", 8, 8);
				}
				if (-1 != pl.getFloatParameter("rate"))
				{
					FacilityManager.getMsgLogger().println(
							"Target rate = " + this.breader.getTargetRate() + " bpp (" + this.breader.getTargetNbytes()
									+ " bytes)", 8, 8);
				}
			}

			// **** Decode and write/display result ****
			if (disp)
			{
				// Now create the image and decode. Use a low priority for
				// this so as not to block other threads.
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY + 1);
				img = BlkImgDataSrcImageProducer.createImage(decodedImage, this.isp);
				this.isp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// HACK for JDK 1.1.x under Windows
				if (null != win)
				{
					this.win.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}
				// END HACK
				this.isp.setImage(img);
				this.isp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				// HACK for JDK 1.1.x under Windows
				if (null != win)
				{
					this.win.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				// END HACK

				// Check the image status, every 100ms, until it is finished.
				if (null != win)
				{
					int status;
					do
					{
						status = this.isp.checkImage(img, null);
						if (0 != (status & ImageObserver.ERROR))
						{
							FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR,
									"An unknown error occurred while producing the image");
							return;
						}
						else if (0 != (status & ImageObserver.ABORT))
						{
							FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR,
									"Image production was aborted for some unknown reason");
						}
						else if (0 != (status & ImageObserver.ALLBITS))
						{
							final ImgMouseListener iml = new ImgMouseListener(this.isp);
							this.isp.addMouseListener(iml);
							this.isp.addMouseMotionListener(iml);
						}
						else
						{ // Check again in 100 ms
							try
							{
								Thread.sleep(100);
							}
							catch (final
							InterruptedException e)
							{
							}
						}
					} while (0 == (status & (ImageObserver.ALLBITS | ImageObserver.ABORT | ImageObserver.ERROR)));
				}
			}
			else
			{
				// Need to optimize! If no component mixer is used and PGM
				// files are written need to write blocks in parallel
				// (otherwise decodes 3 times)

				// Now write the image to the file (decodes as needed)
				for (i = 0; i < imwriter.length; i++)
				{
					if (".PGM".equalsIgnoreCase(outext))
					{
						try
						{
							imwriter[i] = new ImgWriterPGM(out[i], decodedImage, i);
						}
						catch (final IOException e)
						{
							this.error("Cannot write PGM header or open output file for component " + i
									+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
							return;
						}
					}
					else if (".PGX".equalsIgnoreCase(outext))
					{
						// Not PGM and not PPM means PGX used
						try
						{
							if (null != csMap)
							{
								imwriter[i] = new ImgWriterPGX(out[i], decodedImage, i, this.csMap.isOutputSigned(i));
							}
							else
							{
								imwriter[i] = new ImgWriterPGX(out[i], decodedImage, i, this.hd.isOriginalSigned(i));
							}
						}
						catch (final IOException e)
						{
							this.error("Cannot write PGX header or open output file for component " + i
									+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
							return;
						}
					}

					try
					{
						imwriter[i].writeAll();
					}
					catch (final IOException e)
					{
						this.error("I/O error while writing output file"
								+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
						return;
					}
					try
					{
						imwriter[i].close();
					}
					catch (final IOException e)
					{
						this.error("I/O error while closing output file (data may be corrupted"
								+ ((null != e.getMessage()) ? (":\n" + e.getMessage()) : ""), 2, e);
						return;
					}
				}
			}

			// **** Print some resulting info ****
			if (verbose)
			{
				// Print actually read bitrate
				// if file format used add the read file format bytes
				float bitrate = this.breader.getActualRate();
				int numBytes = this.breader.getActualNbytes();
				if (ff.JP2FFUsed)
				{
					final int imageSize = (int) ((8.0f * numBytes) / bitrate);
					numBytes += ff.getFirstCodeStreamPos();
					bitrate = (numBytes * 8.0f) / imageSize;
				}

				if (-1 == pl.getIntParameter("ncb_quit"))
				{
					FacilityManager.getMsgLogger().println(
							"Actual bitrate = " + bitrate + " bpp (i.e. " + numBytes + " bytes)", 8, 8);
				}
				else
				{
					FacilityManager.getMsgLogger().println("Number of packet body bytes read = " + numBytes, 8, 8);
				}
				FacilityManager.getMsgLogger().flush();
			}

		}
		catch (final IllegalArgumentException e)
		{
			this.error(e.getMessage(), 2);
			if ("on".equals(pl.getParameter("debug")))
				e.printStackTrace();
		}
		catch (final Error e)
		{
			if (null != e.getMessage())
			{
				this.error(e.getMessage(), 2);
			}
			else
			{
				this.error("An error has occurred during decoding.", 2);
			}

			if ("on".equals(pl.getParameter("debug")))
			{
				e.printStackTrace();
			}
			else
			{
				this.error("Use '-debug' option for more details", 2);
			}
		}
		catch (final RuntimeException e)
		{
			if (null != e.getMessage())
			{
				this.error("An uncaught runtime exception has occurred:\n" + e.getMessage(), 2);
			}
			else
			{
				this.error("An uncaught runtime exception has occurred.", 2);
			}
			if ("on".equals(pl.getParameter("debug")))
			{
				e.printStackTrace();
			}
			else
			{
				this.error("Use '-debug' option for more details", 2);
			}
		}
		catch (final Throwable e)
		{
			this.error("An uncaught exception has occurred.", 2);
			if ("on".equals(pl.getParameter("debug")))
			{
				e.printStackTrace();
			}
			else
			{
				this.error("Use '-debug' option for more details", 2);
			}
		}
	}

	/**
	 * Prints version and copyright information to the logging facility returned
	 * by FacilityManager.getMsgLogger()
	 */
	private void printVersionAndCopyright()
	{
		FacilityManager.getMsgLogger().println("JJ2000's JPEG 2000 Decoder\n", 2, 4);
		FacilityManager.getMsgLogger().println("Version: " + JJ2KInfo.version + "\n", 2, 4);
		FacilityManager.getMsgLogger().println("Copyright:\n\n" + JJ2KInfo.copyright + "\n", 2, 4);
		FacilityManager.getMsgLogger().println("Send bug reports to: " + JJ2KInfo.bugaddr + "\n", 2, 4);
	}

	/**
	 * Prints the usage information to stdout. The usage information is written
	 * for all modules in the decoder.
	 */
	private void printUsage()
	{
		final MsgLogger ml = FacilityManager.getMsgLogger();

		ml.println("Usage:", 0, 0);
		ml.println("JJ2KDecoder args...\n", 10, 12);
		ml.println("The exit code of the decoder is non-zero if an error occurs.", 2, 4);
		ml.println("The following arguments are recongnized:\n", 2, 4);

		// Print decoding options
		this.printParamInfo(ml, ImgDecoder.getAllParameters());

		// Print bug-report address
		FacilityManager.getMsgLogger().println("\n\n", 0, 0);
		FacilityManager.getMsgLogger().println("Send bug reports to: " + JJ2KInfo.bugaddr + "\n", 2, 4);
	}

	/**
	 * Prints the parameters in 'pinfo' to the provided output, 'out', showing
	 * the existing defaults. The message is printed to the logging facility
	 * returned by FacilityManager.getMsgLogger(). The 'pinfo' argument is a 2D
	 * String array. The first dimension contains String arrays, 1 for each
	 * parameter. Each of these arrays has 3 elements, the first element is the
	 * parameter name, the second element is the synopsis for the parameter and
	 * the third one is a long description of the parameter. If the synopsis or
	 * description is 'null' then no synopsis or description is printed,
	 * respectively. If there is a default value for a parameter it is also
	 * printed.
	 * 
	 * @param out
	 *            Where to print.
	 * 
	 * @param pinfo
	 *            The parameter information to write.
	 */
	private void printParamInfo(final MsgLogger out, final String[][] pinfo)
	{
		String defval;

		for (int i = 0; i < pinfo.length; i++)
		{
			defval = this.defpl.getParameter(pinfo[i][0]);
			if (null != defval)
			{ // There is a default value
				out.println("-" + pinfo[i][0] + ((null != pinfo[i][1]) ? " " + pinfo[i][1] + " " : " ") + "(default = "
						+ defval + ")", 4, 8);
			}
			else
			{ // There is no default value
				out.println("-" + pinfo[i][0] + ((null != pinfo[i][1]) ? " " + pinfo[i][1] : ""), 4, 8);
			}
			// Is there an explanatory message?
			if (null != pinfo[i][2])
			{
				out.println(pinfo[i][2], 6, 6);
			}
		}
	}

	/**
	 * Exit the decoding process according to the isChildProcess variable
	 */
	public void exit()
	{
		if (this.isChildProcess)
		{
			if (null != win)
				this.win.dispose();
			if (null != title)
				this.title.done = true;
			return;
		}
		System.exit(0);
	}

	/**
	 * Set isChildProcess variable.
	 * 
	 * @param b
	 *            The boolean value
	 */
	public void setChildProcess(final boolean b)
	{
		this.isChildProcess = b;
	}
}
