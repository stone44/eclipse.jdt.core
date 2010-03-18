/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

public class BindingKeyParser {

	int keyStart;

	static final char C_THROWN = '|';

	static class Scanner {
		static final int PACKAGE = 0;
		static final int TYPE = 1;
		static final int FIELD = 2;
		static final int METHOD = 3;
		static final int ARRAY = 4;
		static final int LOCAL_VAR = 5;
		static final int FLAGS = 6;
		static final int WILDCARD = 7;
		static final int CAPTURE = 8;
		static final int BASE_TYPE = 9;
		static final int END = 10;

		static final int START = -1;

		int index = 0, start;
		char[] source;
		int token = START;

		Scanner(char[] source) {
			this.source = source;
		}

		char[] getTokenSource() {
			int length = this.index-this.start;
			char[] result = new char[length];
			System.arraycopy(this.source, this.start, result, 0, length);
			return result;
		}

		boolean isAtAnnotationStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == '@';
		}

		boolean isAtCaptureStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == '!';
		}

		boolean isAtFieldOrMethodStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == '.';
		}

		boolean isAtLocalVariableStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == '#';
		}

		boolean isAtMemberTypeStart() {
			return
				this.index < this.source.length
				&& (this.source[this.index] == '$'
					|| (this.source[this.index] == '.' && this.source[this.index-1] == '>'));
		}

		boolean isAtParametersEnd() {
			return
				this.index < this.source.length
					&& this.source[this.index] == '>';
		}

		boolean isAtParametersStart() {
			char currentChar;
			return
				this.index > 0
				&& this.index < this.source.length
				&& ((currentChar = this.source[this.index]) == '<'
					|| currentChar == '%');
		}

		boolean isAtRawTypeEnd() {
			return
				this.index > 0
				&& this.index < this.source.length
				&& this.source[this.index] == '>';
		}

		boolean isAtSecondaryTypeStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == '~';
		}

		boolean isAtWildcardStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == '{';   // e.g {1}+Ljava/lang/String;
		}

		boolean isAtTypeParameterStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == 'T';
		}

		boolean isAtTypeArgumentStart() {
			return this.index < this.source.length && "LIZVCDBFJS[!".indexOf(this.source[this.index]) != -1; //$NON-NLS-1$
		}

		boolean isAtThrownStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == C_THROWN;
		}

		boolean isAtTypeVariableStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == ':';
		}

		boolean isAtTypeWithCaptureStart() {
			return
				this.index < this.source.length
				&& this.source[this.index] == '&';
		}

		int nextToken() {
			int previousTokenEnd = this.index;
			this.start = this.index;
			int dollarIndex = -1;
			int length = this.source.length;
			while (this.index <= length) {
				char currentChar = this.index == length ? Character.MIN_VALUE : this.source[this.index];
				switch (currentChar) {
					case 'B':
					case 'C':
					case 'D':
					case 'F':
					case 'I':
					case 'J':
					case 'N':
					case 'S':
					case 'V':
					case 'Z':
						// base type
						if (this.index == previousTokenEnd
								&& (this.index == 0 || this.source[this.index-1] != '.')) { // case of field or method starting with one of the character above
							this.index++;
							this.token = BASE_TYPE;
							return this.token;
						}
						break;
					case 'L':
					case 'T':
						if (this.index == previousTokenEnd
								&& (this.index == 0 || this.source[this.index-1] != '.')) { // case of field or method starting with one of the character above
							this.start = this.index+1;
							dollarIndex = -1;
						}
						break;
					case ';':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							dollarIndex = -1;
							previousTokenEnd = this.start;
						} else {
							if (dollarIndex != -1) this.index = dollarIndex;
							this.token = TYPE;
							return this.token;
						}
						break;
					case '$':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							dollarIndex = -1;
						} else {
							if (dollarIndex == -1) {
								dollarIndex = this.index;
								break;
							}
							this.index = dollarIndex;
							this.token = TYPE;
							return this.token;
						}
						break;
					case '~':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							dollarIndex = -1;
						} else {
							this.token = TYPE;
							return this.token;
						}
						break;
					case '.':
					case '%':
					case ':':
					case '>':
					case '@':
						this.start = this.index+1;
						dollarIndex = -1;
						previousTokenEnd = this.start;
						break;
					case '[':
						while (this.index < length && this.source[this.index] == '[')
							this.index++;
						this.token = ARRAY;
						return this.token;
					case '<':
						if (this.start > 0) {
							switch (this.source[this.start-1]) {
								case '.':
									if (this.source[this.start-2] == '>') {
										// case of member type where enclosing type is parameterized
										if (dollarIndex != -1) this.index = dollarIndex;
										this.token = TYPE;
									} else {
										this.token = METHOD;
									}
									return this.token;
								default:
									if (this.index == previousTokenEnd) {
										this.start = this.index+1;
										dollarIndex = -1;
										previousTokenEnd = this.start;
									} else {
										if (dollarIndex != -1) this.index = dollarIndex;
										this.token = TYPE;
										return this.token;
									}
							}
						}
						break;
					case '(':
						this.token = METHOD;
						return this.token;
					case ')':
						if (this.token == TYPE) {
							this.token = FIELD;
							return this.token;
						}
						this.start = this.index+1;
						dollarIndex = -1;
						previousTokenEnd = this.start;
						break;
					case '#':
						if (this.index == previousTokenEnd) {
							this.start = this.index+1;
							dollarIndex = -1;
							previousTokenEnd = this.start;
						} else {
							this.token = LOCAL_VAR;
							return this.token;
						}
						break;
					case Character.MIN_VALUE:
						switch (this.token) {
							case START:
								this.token = PACKAGE;
								break;
							case METHOD:
							case LOCAL_VAR:
								this.token = LOCAL_VAR;
								break;
							case TYPE:
								if (this.index > this.start && this.source[this.start-1] == '.')
									this.token = FIELD;
								else
									this.token = END;
								break;
							case WILDCARD:
								this.token = TYPE;
								break;
							default:
								this.token = END;
								break;
						}
						return this.token;
					case '*':
					case '+':
					case '-':
						this.index++;
						this.token = WILDCARD;
						return this.token;
					case '!':
					case '&':
						this.index++;
						this.token = CAPTURE;
						return this.token;
				}
				this.index++;
			}
			this.token = END;
			return this.token;
		}

		void skipMethodSignature() {
			this.start = this.index;
			int braket = 0;
			while (this.index < this.source.length) {
				switch (this.source[this.index]) {
					case '#':
					case '%':
					case '@':
					case C_THROWN:
						return;
					case ':':
						if (braket == 0)
							return;
						break;
					case '<':
					case '(':
						braket++;
						break;
					case '>':
					case ')':
						braket--;
						break;
				}
				this.index++;
			}
		}

		void skipRank() {
			this.start = this.index;
			while (this.index < this.source.length && "0123456789".indexOf(this.source[this.index]) != -1) //$NON-NLS-1$
				this.index++;
		}
		
		void skipThrownStart() {
			while (this.index < this.source.length && this.source[this.index] == C_THROWN)
				this.index++;
		}

		void skipParametersStart() {
			while (this.index < this.source.length && (this.source[this.index] == '<' || this.source[this.index] == '%'))
				this.index++;
		}

		void skipParametersEnd() {
			while (this.index < this.source.length && this.source[this.index] != '>')
				this.index++;
			this.index++;
		}

		void skipTypeEnd() {
			if (this.index < this.source.length && this.source[this.index] == ';')
				this.index++;
		}
		
		void skipRankStart() {
			if (this.index < this.source.length && this.source[this.index] == '{')
				this.index++;
		}
		
		void skipRankEnd() {
			if (this.index < this.source.length && this.source[this.index] == '}')
				this.index++;
			this.start = this.index;
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			switch (this.token) {
				case START:
					buffer.append("START: "); //$NON-NLS-1$
					break;
				case PACKAGE:
					buffer.append("PACKAGE: "); //$NON-NLS-1$
					break;
				case TYPE:
					buffer.append("TYPE: "); //$NON-NLS-1$
					break;
				case FIELD:
					buffer.append("FIELD: "); //$NON-NLS-1$
					break;
				case METHOD:
					buffer.append("METHOD: "); //$NON-NLS-1$
					break;
				case ARRAY:
					buffer.append("ARRAY: "); //$NON-NLS-1$
					break;
				case LOCAL_VAR:
					buffer.append("LOCAL VAR: "); //$NON-NLS-1$
					break;
				case FLAGS:
					buffer.append("MODIFIERS: "); //$NON-NLS-1$
					break;
				case WILDCARD:
					buffer.append("WILDCARD: "); //$NON-NLS-1$
					break;
				case CAPTURE:
					buffer.append("CAPTURE: "); //$NON-NLS-1$
					break;
				case BASE_TYPE:
					buffer.append("BASE TYPE: "); //$NON-NLS-1$
					break;
				case END:
					buffer.append("END: "); //$NON-NLS-1$
					break;
			}
			if (this.index < 0) {
				buffer.append("**"); //$NON-NLS-1$
				buffer.append(this.source);
			} else if (this.index <= this.source.length) {
				buffer.append(this.source, 0, this.start);
				buffer.append('*');
				if (this.start <= this.index) {
					buffer.append(this.source, this.start, this.index - this.start);
					buffer.append('*');
					buffer.append(this.source, this.index, this.source.length - this.index);
				} else {
					buffer.append('*');
					buffer.append(this.source, this.start, this.source.length - this.start);
				}
			} else {
				buffer.append(this.source);
				buffer.append("**"); //$NON-NLS-1$
			}
			return buffer.toString();
		}
	}
	private boolean parsingPaused;

	private Scanner scanner;

	private boolean hasTypeName = true;

	private boolean isMalformed;

	public BindingKeyParser(BindingKeyParser parser) {
		this(""); //$NON-NLS-1$
		this.scanner = parser.scanner;
	}

	public BindingKeyParser(String key) {
		this.scanner = new Scanner(key.toCharArray());
	}

	public void consumeAnnotation() {
		// default is to do nothing
	}

	public void consumeArrayDimension(char[] brakets) {
		// default is to do nothing
	}

	public void consumeBaseType(char[] baseTypeSig) {
		// default is to do nothing
	}

	public void consumeCapture(int position) {
		// default is to do nothing
	}

	public void consumeException() {
		// default is to do nothing
	}

	public void consumeField(char[] fieldName) {
		// default is to do nothing
	}

	public void consumeParameterizedGenericMethod() {
		// default is to do nothing
	}

	public void consumeLocalType(char[] uniqueKey) {
		// default is to do nothing
	}

	public void consumeLocalVar(char[] varName, int occurrenceCount) {
		// default is to do nothing
	}

	public void consumeMethod(char[] selector, char[] signature) {
		// default is to do nothing
	}

	public void consumeModifiers(char[] modifiers) {
		// default is to do nothing
	}

	public void consumeNonGenericType() {
		// default is to do nothing
	}

	public void consumeMemberType(char[] simpleTypeName) {
		// default is to do nothing
	}

	public void consumePackage(char[] pkgName) {
		// default is to do nothing
	}

	public void consumeParameterizedType(char[] simpleTypeName, boolean isRaw) {
		// default is to do nothing
	}

	public void consumeParser(BindingKeyParser parser) {
		// default is to do nothing
	}

	public void consumeRawType() {
		// default is to do nothing
	}

	public void consumeScope(int scopeNumber) {
		// default is to do nothing
	}

	public void consumeSecondaryType(char[] simpleTypeName) {
		// default is to do nothing
	}

	public void consumeFullyQualifiedName(char[] fullyQualifiedName) {
		// default is to do nothing
	}

	public void consumeKey() {
		// default is to do nothing
	}

	public void consumeTopLevelType() {
		// default is to do nothing
	}

	public void consumeType() {
		// default is to do nothing
	}

	public void consumeTypeParameter(char[] typeParameterName) {
		// default is to do nothing
	}

	public void consumeTypeVariable(char[] position, char[] typeVariableName) {
		// default is to do nothing
	}

	public void consumeTypeWithCapture() {
		// default is to do nothing
	}

	public void consumeWildCard(int kind) {
		// default is to do nothing
	}
	
	public void consumeWildcardRank(int rank) {
		// default is to do nothing
	}

	/*
	 * Returns the string that this binding key wraps.
	 */
	public String getKey() {
		return new String(this.scanner.source);
	}

	public boolean hasTypeName() {
		return this.hasTypeName;
	}

	public void malformedKey() {
		this.isMalformed = true;
	}

	public BindingKeyParser newParser() {
		return new BindingKeyParser(this);
	}

	public void parse() {
		parse(false/*don't pause after fully qualified name*/);
	}

	public void parse(boolean pauseAfterFullyQualifiedName) {
		if (!this.parsingPaused) {
			// fully qualified name
			parseFullyQualifiedName();
			parseSecondaryType();
			if (pauseAfterFullyQualifiedName) {
				this.parsingPaused = true;
				return;
			}
		}
		if (!hasTypeName()) {
			consumeKey();
			return;
		}
		consumeTopLevelType();
		parseInnerType();

		if (this.scanner.isAtParametersStart()) {
			this.scanner.skipParametersStart();
			if (this.scanner.isAtTypeParameterStart())	{
				// generic type
				parseGenericType();
			 	// skip ";>"
			 	this.scanner.skipParametersEnd();
				// local type in generic type
				parseInnerType();
			} else if (this.scanner.isAtTypeArgumentStart())
				// parameterized type
				parseParameterizedType(null/*top level type or member type with raw enclosing type*/, false/*no raw*/);
			else if (this.scanner.isAtRawTypeEnd())
				// raw type
				parseRawType();
		} else {
			// non-generic type
			consumeNonGenericType();
		}

		consumeType();
		this.scanner.skipTypeEnd();

		if (this.scanner.isAtFieldOrMethodStart()) {
			switch (this.scanner.nextToken()) {
				case Scanner.FIELD:
					parseField();
					if (this.scanner.isAtAnnotationStart()) {
						parseAnnotation();
					}
					return;
				case Scanner.METHOD:
					parseMethod();
					if (this.scanner.isAtLocalVariableStart()) {
						parseLocalVariable();
					} else if (this.scanner.isAtTypeVariableStart()) {
						parseTypeVariable();
					} else if (this.scanner.isAtAnnotationStart()) {
						parseAnnotation();
					}
			 		break;
				default:
					malformedKey();
					return;
			}
		} else if (this.scanner.isAtTypeVariableStart()) {
			parseTypeVariable();
		} else if (this.scanner.isAtWildcardStart()) {
			parseWildcard();
		} else if (this.scanner.isAtTypeWithCaptureStart()) {
			parseTypeWithCapture();
		} else if (this.scanner.isAtAnnotationStart()) {
			parseAnnotation();
		}

		consumeKey();
	}

	private void parseFullyQualifiedName() {
		if (this.scanner.isAtCaptureStart()) {
			parseCapture();
			this.hasTypeName = false;
			return;
		}
		switch(this.scanner.nextToken()) {
			case Scanner.PACKAGE:
				this.keyStart = 0;
				consumePackage(this.scanner.getTokenSource());
				this.hasTypeName = false;
				return;
			case Scanner.TYPE:
				this.keyStart = this.scanner.start-1;
				consumeFullyQualifiedName(this.scanner.getTokenSource());
				break;
			case Scanner.BASE_TYPE:
				this.keyStart = this.scanner.start-1;
				consumeBaseType(this.scanner.getTokenSource());
				this.hasTypeName = false;
				break;
			case Scanner.ARRAY:
				this.keyStart = this.scanner.start;
				consumeArrayDimension(this.scanner.getTokenSource());
				switch (this.scanner.nextToken()) {
					case Scanner.TYPE:
						consumeFullyQualifiedName(this.scanner.getTokenSource());
						break;
					case Scanner.BASE_TYPE:
						consumeBaseType(this.scanner.getTokenSource());
						this.hasTypeName = false;
						break;
					default:
						malformedKey();
						return;
				}
				break;
			default:
				malformedKey();
				return;
		}
	}

	private void parseParameterizedMethod() {
		this.scanner.skipParametersStart();
		while (!this.scanner.isAtParametersEnd() && !this.isMalformed) {
			parseTypeArgument();
		}
		consumeParameterizedGenericMethod();
	}

	private void parseGenericType() {
		while (!this.scanner.isAtParametersEnd() && !this.isMalformed) {
			if (this.scanner.nextToken() != Scanner.TYPE) {
				malformedKey();
				return;
			}
			consumeTypeParameter(this.scanner.getTokenSource());
			this.scanner.skipTypeEnd();
		}
	}

	private void parseInnerType() {
		if (!this.scanner.isAtMemberTypeStart() || this.scanner.nextToken() != Scanner.TYPE)
			return;
		char[] typeName = this.scanner.getTokenSource();
		if (Character.isDigit(typeName[0])) {
			// anonymous or local type
			int nextToken = Scanner.TYPE;
			while (this.scanner.isAtMemberTypeStart() && !this.isMalformed)
				nextToken = this.scanner.nextToken();
			typeName = nextToken == Scanner.END ? this.scanner.source : CharOperation.subarray(this.scanner.source, this.keyStart, this.scanner.index+1);
			consumeLocalType(typeName);
		} else {
			consumeMemberType(typeName);
			parseInnerType();
		}
	}

	private void parseLocalVariable() {
		if (this.scanner.nextToken() != Scanner.LOCAL_VAR) {
			malformedKey();
			return;
		}
		char[] varName = this.scanner.getTokenSource();
		if (Character.isDigit(varName[0])) {
			int index = Integer.parseInt(new String(varName));
			consumeScope(index);
			if (!this.scanner.isAtLocalVariableStart()) {
				malformedKey();
				return;
			}
			parseLocalVariable();
		} else {
			int occurrenceCount = 0;
			if (this.scanner.isAtLocalVariableStart()) {
				if (this.scanner.nextToken() != Scanner.LOCAL_VAR) {
					malformedKey();
					return;
				}
				char[] occurrence = this.scanner.getTokenSource();
				occurrenceCount = Integer.parseInt(new String(occurrence));
			}
			consumeLocalVar(varName, occurrenceCount);
		}
	}

	private void parseMethod() {
		char[] selector = this.scanner.getTokenSource();
		this.scanner.skipMethodSignature();
		char[] signature = this.scanner.getTokenSource();
		consumeMethod(selector, signature);
		if (this.scanner.isAtThrownStart()) {
			parseThrownExceptions();
		}
		if (this.scanner.isAtParametersStart())
			parseParameterizedMethod();
	}

	private void parseAnnotation() {
		/*
		 * The call parser.parse() might have a side-effect on the current token type
		 * See bug 264443
		 */
		int token = this.scanner.token;
		BindingKeyParser parser = newParser();
		parser.parse();
		consumeParser(parser);
		consumeAnnotation();
		this.isMalformed = parser.isMalformed;
		this.scanner.token = token;
	}

	private void parseCapture() {
		if (this.scanner.nextToken() != Scanner.CAPTURE) return;
		parseCaptureWildcard();
		if (this.scanner.nextToken() != Scanner.TYPE) {
			malformedKey();
			return;
		}
		char[] positionChars = this.scanner.getTokenSource();
		int position = Integer.parseInt(new String(positionChars));
		consumeCapture(position);
		this.scanner.skipTypeEnd();
	}

	private void parseCaptureWildcard() {
		/*
		 * The call parser.parse() might have a side-effect on the current token type
		 * See bug 264443
		 */
		int token = this.scanner.token;
		BindingKeyParser parser = newParser();
		parser.parse();
		consumeParser(parser);
		this.isMalformed = parser.isMalformed;
		this.scanner.token = token;
	}

	private void parseField() {
		char[] fieldName = this.scanner.getTokenSource();
		parseReturnType();
 		consumeField(fieldName);
	}

	private void parseThrownExceptions() {
		/*
		 * The call parser.parse() might have a side-effect on the current token type
		 * See bug 264443
		 */
		int token = this.scanner.token;
		while (this.scanner.isAtThrownStart() && !this.isMalformed) {
			this.scanner.skipThrownStart();
			BindingKeyParser parser = newParser();
			parser.parse();
			consumeParser(parser);
			consumeException();
			this.isMalformed = parser.isMalformed;
		}
		this.scanner.token = token;
	}

	private void parseParameterizedType(char[] typeName, boolean isRaw) {
		if (!isRaw) {
			while (!this.scanner.isAtParametersEnd() && !this.isMalformed) {
				parseTypeArgument();
			}
		}
	 	// skip ";>"
	 	this.scanner.skipParametersEnd();
		consumeParameterizedType(typeName, isRaw);
		this.scanner.skipTypeEnd();
	 	if (this.scanner.isAtMemberTypeStart() && this.scanner.nextToken() == Scanner.TYPE) {
	 		typeName = this.scanner.getTokenSource();
			if (this.scanner.isAtParametersStart()) {
				this.scanner.skipParametersStart();
		 		parseParameterizedType(typeName, this.scanner.isAtRawTypeEnd());
			} else
				consumeParameterizedType(typeName, true/*raw*/);
	 	}
	}

	private void parseRawType() {
		this.scanner.skipParametersEnd();
		consumeRawType();
		this.scanner.skipTypeEnd();
	 	if (this.scanner.isAtMemberTypeStart() && this.scanner.nextToken() == Scanner.TYPE) {
	 		char[] typeName = this.scanner.getTokenSource();
			if (this.scanner.isAtParametersStart()) {
				this.scanner.skipParametersStart();
		 		parseParameterizedType(typeName, this.scanner.isAtRawTypeEnd());
			} else
				consumeParameterizedType(typeName, true/*raw*/);
	 	}
	}

	private void parseReturnType() {
		this.scanner.index++; // skip ')'
		/*
		 * The call parser.parse() might have a side-effect on the current token type
		 * See bug 264443
		 */
		int token = this.scanner.token;
		BindingKeyParser parser = newParser();
		parser.parse();
		consumeParser(parser);
		this.isMalformed = parser.isMalformed;
		this.scanner.token = token;
	}

	private void parseSecondaryType() {
		if (!this.scanner.isAtSecondaryTypeStart() || this.scanner.nextToken() != Scanner.TYPE) return;
		consumeSecondaryType(this.scanner.getTokenSource());
	}

	private void parseTypeArgument() {
		/*
		 * The call parser.parse() might have a side-effect on the current token type
		 * See bug 264443
		 */
		int token = this.scanner.token;
		BindingKeyParser parser = newParser();
		parser.parse();
		consumeParser(parser);
		this.isMalformed = parser.isMalformed;
		this.scanner.token = token;
	}

	private void parseTypeWithCapture() {
		if (this.scanner.nextToken() != Scanner.CAPTURE) return;
		/*
		 * The call parser.parse() might have a side-effect on the current token type
		 * See bug 264443
		 */
		int token = this.scanner.token;
		BindingKeyParser parser = newParser();
		parser.parse();
		consumeParser(parser);
		consumeTypeWithCapture();
		this.isMalformed = parser.isMalformed;
		this.scanner.token = token;
	}

	private void parseTypeVariable() {
		if (this.scanner.nextToken() != Scanner.TYPE) {
			malformedKey();
			return;
		}
		char[] typeVariableName = this.scanner.getTokenSource();
		char[] position;
		int length = typeVariableName.length;
		if (length > 0 && Character.isDigit(typeVariableName[0])) {
			int firstT = CharOperation.indexOf('T', typeVariableName);
			position = CharOperation.subarray(typeVariableName, 0, firstT);
			typeVariableName = CharOperation.subarray(typeVariableName, firstT+1, typeVariableName.length);
		} else {
			position = CharOperation.NO_CHAR;
		}
		consumeTypeVariable(position, typeVariableName);
		this.scanner.skipTypeEnd();
	}

	private void parseWildcard() {
		parseWildcardRank();
		if (this.scanner.nextToken() != Scanner.WILDCARD) return;
		char[] source = this.scanner.getTokenSource();
		if (source.length == 0) {
			malformedKey();
			return;
		}
		int kind = -1;
		switch (source[0]) {
			case '*':
				kind = Wildcard.UNBOUND;
				break;
			case '+':
				kind = Wildcard.EXTENDS;
				break;
			case '-':
				kind = Wildcard.SUPER;
				break;
		}
		if (kind == -1) {
			malformedKey();
			return;
		}
		if (kind != Wildcard.UNBOUND)
			parseWildcardBound();
		consumeWildCard(kind);
	}

	private void parseWildcardRank() {
		this.scanner.skipRankStart();
		this.scanner.skipRank();
		char[] source = this.scanner.getTokenSource();
		consumeWildcardRank(Integer.parseInt(new String(source)));
		this.scanner.skipRankEnd();
	}
	
	private void parseWildcardBound() {
		/*
		 * The call parser.parse() might have a side-effect on the current token type
		 * See bug 264443
		 */
		int token = this.scanner.token;
		BindingKeyParser parser = newParser();
		parser.parse();
		consumeParser(parser);
		this.isMalformed = parser.isMalformed;
		this.scanner.token = token;
	}

}
