/*
 * $RCSfile: J2KImageReadParam.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.3 $
 * $Date: 2006/09/29 19:25:32 $
 * $State: Exp $
 */
package jj2000.j2k.decoder;

import javax.imageio.ImageReadParam;

/**
 * A subclass of {@code ImageReadParam} for reading images in
 * the JPEG 2000 format.
 *
 * <p>The decoding parameters for JPEG 2000 are listed below:
 *
 * <p><table border=1>
 * <caption><b>JPEG 2000 Plugin Decoding Parameters</b></caption>
 * <tr><th>Parameter Name</th> <th>Description</th></tr>
* <tr>
 *    <td>decodingRate</td>
 *    <td>Specifies the decoding rate in bits per pixel (bpp) where the
 *    number of pixels is related to the image's original size (Note:
 *    this parameter is not affected by {@code resolution}).  The
 *    codestream is either parsed (default) or truncated depending
 *    {@code parsingEnabled}.  The default is <code>Double.MAX_VALUE</code>.
 *    It means decoding with the encoding rate.
 *    </td>
 * </tr>
 * <tr>
 *    <td>resolution</td>
 *    <td>Specifies the resolution level wanted for the decoded image
 *    (0 means the lowest available resolution, the resolution
 *    level gives an image with the original dimension).  If the given index
 *    is greater than the number of available resolution levels of the
 *    compressed image, the decoded image has the lowest available
 *    resolution (among all tile-components).  This parameter affects only
 *    the inverse wavelet transform and not the number of bytes read by the
 *    codestream parser, which depends only on {@code decodingRate}.
 *    </td>
 * </tr>
 * <tr>
 *    <td>noROIDescaling</td>
 *    <td>Ensures that no ROI de-scaling is performed.  Decompression is done
 *    like there is no ROI in the image.
 *    </td>
 * </tr>
 * <tr>
 *    <td>parsingEnabled</td>
 *    <td>Enable the parsing mode or not when the decoding rate is specified.
 *    If it is false, the codestream is decoded as if it were truncated to
 *    the given rate.  If it is true, the decoder creates, truncates and
 *    decodes a virtual layer progressive codestream with the same
 *    truncation points in each code-block.
 *    </td>
 * </tr>
 * </table>
 */
public class DecoderParam extends ImageReadParam {
    /** Specifies the decoding rate in bits per pixel (bpp) where the
     *  number of  pixels is related to the image's original size
     *  (Note: this number is not affected by {@code resolution}).
     */
    private double decodingRate = Double.MAX_VALUE;

    /** Specifies the resolution level wanted for the decoded image
     *  (0 means the lowest available resolution, the resolution
     *  level gives an image with the original dimension).  If the given index
     *  is greater than the number of available resolution levels of the
     *  compressed image, the decoded image has the lowest available
     *  resolution (among all tile-components).  This parameter
     *  affects only the inverse wavelet transform but not the number
     *  of bytes read by the codestream parser, which
     *  depends only on {@code decodingRate}.
     */
    private int resolution = -1;

    /** Ensures that no ROI de-scaling is performed.  Decompression
     *  is done like there is no ROI in the image.
     */
    private boolean noROIDescaling = true;

    /** Enable the parsing mode or not when the decoding rate is specified .
     *  If it is false, the codestream is decoded as if it were truncated to
     *  the given rate.  If it is true, the decoder creates, truncates and
     *  decodes a virtual layer progressive codestream with the same
     *  truncation points in each code-block.
     */
    private boolean parsingEnabled = true;

    /** Constructs a default instance of {@code J2KImageReadParam}. */
    public DecoderParam() {
	}

    /**
     * Sets {@code decodingRate}.
     *
     * @param rate the decoding rate in bits per pixel.
     * @see #getDecodingRate()
     */
    public void setDecodingRate(final double rate) {
        decodingRate = rate;
    }

    /**
     * Gets {@code decodingRate}.
     *
     * @return the decoding rate in bits per pixel.
     * @see #setDecodingRate(double)
     */
    public double getDecodingRate() {
        return this.decodingRate;
    }

    /**
     * Sets {@code resolution}.
     *
     * @param resolution the resolution level with 0 being
     * the lowest available.
     * @see #getResolution()
     */
    public void setResolution(final int resolution) {
        this.resolution = Math.max(resolution, -1);
    }

    /**
     * Gets {@code resolution}.
     *
     * @return the resolution level with 0 being
     * the lowest available.
     * @see #setResolution(int)
     */
    public int getResolution() {
        return this.resolution;
    }

    /** Sets {@code noROIDescaling} */
    public void setNoROIDescaling(final boolean value) {
        noROIDescaling = value;
    }

    /** Gets {@code noROIDescaling} */
    public boolean getNoROIDescaling() {
        return this.noROIDescaling;
    }

    /** Sets {@code parsingEnabled} */
    public void setParsingEnabled(final boolean value) {
        parsingEnabled = value;
    }

    /** Gets {@code parsingEnabled} */
    public boolean getParsingEnabled() {
        return this.parsingEnabled;
    }
}
