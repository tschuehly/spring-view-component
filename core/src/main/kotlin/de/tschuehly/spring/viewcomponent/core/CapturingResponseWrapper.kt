package de.tschuehly.spring.viewcomponent.core

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.ProtoContainer

internal class CapturingResponseWrapper(response: HttpServletResponse) : HttpServletResponseWrapper(response) {
    private val capture: ByteArrayOutputStream
    private var output: ServletOutputStream? = null
    private var writer: PrintWriter? = null
    lateinit var viewComponentBean: Any
    init {
        capture = ByteArrayOutputStream(response.bufferSize)
    }

    override fun getOutputStream(): ServletOutputStream {
        check(writer == null) { "getWriter() has already been called on this response." }
        if (output == null) {
            output = object : ServletOutputStream() {
                override fun isReady(): Boolean {
                    return true
                }

                override fun setWriteListener(writeListener: WriteListener) {}
                @Throws(IOException::class)
                override fun write(b: Int) {
                    capture.write(b)
                }

                @Throws(IOException::class)
                override fun flush() {
                    capture.flush()
                }

                @Throws(IOException::class)
                override fun close() {
                    capture.close()
                }
            }
        }
        return output!!
    }

    @Throws(IOException::class)
    override fun getWriter(): PrintWriter {
        check(output == null) { "getOutputStream() has already been called on this response." }
        if (writer == null) {
            writer = PrintWriter(OutputStreamWriter(capture, characterEncoding))
        }
        return writer!!
    }

    @Throws(IOException::class)
    override fun flushBuffer() {
        super.flushBuffer()
        if (writer != null) {
            writer!!.flush()
        } else if (output != null) {
            output!!.flush()
        }
    }

    @get:Throws(IOException::class)
    val captureAsBytes: ByteArray
        get() {
            if (writer != null) {
                writer!!.close()
            } else if (output != null) {
                output!!.close()
            }
            return capture.toByteArray()
        }

    @get:Throws(IOException::class)
    val captureAsString: String
        get() = String(captureAsBytes, charset(characterEncoding))
}