package com.tryspeed.server;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

class CapturingResponseWrapper extends HttpServletResponseWrapper {

	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private final ServletOutputStream outputStream;
	private PrintWriter writer;
	private int capturedStatus = HttpServletResponse.SC_OK;
	private String capturedContentType;

	CapturingResponseWrapper(HttpServletResponse response) throws IOException {
		super(response);

		ServletOutputStream parentStream = response.getOutputStream();

		this.outputStream = new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				buffer.write(b);
				parentStream.write(b);
			}

			@Override
			public void flush() throws IOException {parentStream.flush();}

			@Override
			public boolean isReady() {return parentStream.isReady();}

			@Override
			public void setWriteListener(WriteListener l) {}
		};
	}

	@Override
	public ServletOutputStream getOutputStream() {return outputStream;}

	@Override
	public PrintWriter getWriter() {
		if (writer == null) {
			String enc = getCharacterEncoding();
			writer = new PrintWriter(new OutputStreamWriter(outputStream, enc != null ? java.nio.charset.Charset.forName(enc) : StandardCharsets.UTF_8));
		}
		return writer;
	}

	@Override
	public void setStatus(int sc) {
		this.capturedStatus = sc;
		super.setStatus(sc);
	}

	@Override
	public void setContentType(String type) {
		this.capturedContentType = type;
		super.setContentType(type);
	}

	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) {
			writer.flush();
		}
		outputStream.flush();
	}

	byte[] getContent() {
		if (writer != null) {
			writer.flush();
		}
		return buffer.toByteArray();
	}

	int getCapturedStatus() {return capturedStatus;}

	String getCapturedContentType() {return capturedContentType;}
}
