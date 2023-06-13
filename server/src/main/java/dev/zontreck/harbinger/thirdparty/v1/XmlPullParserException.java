/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package dev.zontreck.harbinger.thirdparty.v1;

/**
 * This exception is thrown to signal XML Pull Parser related faults.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XmlPullParserException extends Exception {
    protected Throwable detail;
    protected int row = -1;
    protected int column = -1;

    public XmlPullParserException(final String s) {
        super(s);
    }

    /*
    public XmlPullParserException(String s, Throwable thrwble) {
        super(s);
        this.detail = thrwble;
        }

    public XmlPullParserException(String s, int row, int column) {
        super(s);
        this.row = row;
        this.column = column;
    }
    */

    public XmlPullParserException(final String msg, final XmlPullParser parser, final Throwable chain) {
        super ((null == msg ? "" : msg+" ")
               + (null == parser ? "" : "(position:"+parser.getPositionDescription()+") ")
               + (null == chain ? "" : "caused by: "+chain));

        if (null != parser) {
            row = parser.getLineNumber();
            column = parser.getColumnNumber();
        }
        detail = chain;
    }

    public Throwable getDetail() { return this.detail; }
    //    public void setDetail(Throwable cause) { this.detail = cause; }
    public int getLineNumber() { return this.row; }
    public int getColumnNumber() { return this.column; }

    /*
    public String getMessage() {
        if(detail == null)
            return super.getMessage();
        else
            return super.getMessage() + "; nested exception is: \n\t"
                + detail.getMessage();
    }
    */

    //NOTE: code that prints this and detail is difficult in J2ME
    public void printStackTrace() {
        if (null == detail) {
            super.printStackTrace();
        } else {
            synchronized(System.err) {
                System.err.println(getMessage() + "; nested exception is:");
				this.detail.printStackTrace();
            }
        }
    }

}

