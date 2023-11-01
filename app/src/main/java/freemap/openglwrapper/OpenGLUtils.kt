package freemap.openglwrapper

import android.content.res.AssetManager
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object OpenGLUtils {

    fun makeFloatBuffer(vertices: FloatArray): FloatBuffer {
        val bbuf = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
        bbuf.order(ByteOrder.nativeOrder())
        val fbuf = bbuf.asFloatBuffer()
        fbuf.put(vertices)
        fbuf.position(0)
        return fbuf
    }

    fun makeShortBuffer(values: ShortArray): ShortBuffer {
        val bbuf = ByteBuffer.allocateDirect(values.size * Short.SIZE_BYTES)
        bbuf.order(ByteOrder.nativeOrder())
        val sbuf = bbuf.asShortBuffer()
        sbuf.put(values)
        sbuf.position(0)
        return sbuf
    }

    fun loadShader(assets: AssetManager, filename: String): String {
        val file = assets.open(filename)
        val reader = BufferedReader(InputStreamReader(file))
        var code = ""
        var line = reader.readLine()
        while (line != null) {
            code += line
            line = reader.readLine()
        }
        return code
    }

}