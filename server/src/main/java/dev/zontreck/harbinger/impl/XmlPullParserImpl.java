package dev.zontreck.harbinger.impl;

import dev.zontreck.harbinger.thirdparty.v1.XmlPullParser;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class XmlPullParserImpl implements XmlPullParser {

	@Override
	public void setFeature ( String name , boolean state ) throws XmlPullParserException {

	}

	@Override
	public boolean getFeature ( String name ) {
		return false;
	}

	@Override
	public void setProperty ( String name , Object value ) throws XmlPullParserException {

	}

	@Override
	public Object getProperty ( String name ) {
		return null;
	}

	@Override
	public void setInput ( Reader in ) throws XmlPullParserException {

	}

	@Override
	public void setInput ( InputStream inputStream , String inputEncoding ) throws XmlPullParserException {

	}

	@Override
	public String getInputEncoding ( ) {
		return null;
	}

	@Override
	public void defineEntityReplacementText ( String entityName , String replacementText ) throws XmlPullParserException {

	}

	@Override
	public int getNamespaceCount ( int depth ) throws XmlPullParserException {
		return 0;
	}

	@Override
	public String getNamespacePrefix ( int pos ) throws XmlPullParserException {
		return null;
	}

	@Override
	public String getNamespaceUri ( int pos ) throws XmlPullParserException {
		return null;
	}

	@Override
	public String getNamespace ( String prefix ) {
		return null;
	}

	@Override
	public int getDepth ( ) {
		return 0;
	}

	@Override
	public String getPositionDescription ( ) {
		return null;
	}

	@Override
	public int getLineNumber ( ) {
		return 0;
	}

	@Override
	public int getColumnNumber ( ) {
		return 0;
	}

	@Override
	public boolean isWhitespace ( ) throws XmlPullParserException {
		return false;
	}

	@Override
	public String getText ( ) {
		return null;
	}

	@Override
	public char[] getTextCharacters ( int[] holderForStartAndLength ) {
		return new char[ 0 ];
	}

	@Override
	public String getNamespace ( ) {
		return null;
	}

	@Override
	public String getName ( ) {
		return null;
	}

	@Override
	public String getPrefix ( ) {
		return null;
	}

	@Override
	public boolean isEmptyElementTag ( ) throws XmlPullParserException {
		return false;
	}

	@Override
	public int getAttributeCount ( ) {
		return 0;
	}

	@Override
	public String getAttributeNamespace ( int index ) {
		return null;
	}

	@Override
	public String getAttributeName ( int index ) {
		return null;
	}

	@Override
	public String getAttributePrefix ( int index ) {
		return null;
	}

	@Override
	public String getAttributeType ( int index ) {
		return null;
	}

	@Override
	public boolean isAttributeDefault ( int index ) {
		return false;
	}

	@Override
	public String getAttributeValue ( int index ) {
		return null;
	}

	@Override
	public String getAttributeValue ( String namespace , String name ) {
		return null;
	}

	@Override
	public int getEventType ( ) throws XmlPullParserException {
		return 0;
	}

	@Override
	public int next ( ) throws XmlPullParserException, IOException {
		return 0;
	}

	@Override
	public int nextToken ( ) throws XmlPullParserException, IOException {
		return 0;
	}

	@Override
	public void require ( int type , String namespace , String name ) throws XmlPullParserException, IOException {

	}

	@Override
	public String nextText ( ) throws XmlPullParserException, IOException {
		return null;
	}

	@Override
	public int nextTag ( ) throws XmlPullParserException, IOException {
		return 0;
	}

	@Override
	public void skipSubTree ( ) throws XmlPullParserException, IOException {

	}
}
