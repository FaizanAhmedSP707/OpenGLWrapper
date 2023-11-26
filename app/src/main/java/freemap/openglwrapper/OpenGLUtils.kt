package freemap.openglwrapper

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object OpenGLUtils {
    // http://stackoverflow.com/questions/6414003/using-surfacetexture-in-android
    val GL_TEXTURE_EXTERNAL_OES = 0x8d65

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
            code += "${line}\n"
            line = reader.readLine()
        }
        return code
    }

    fun genTextures(n: Int = 1): IntArray {
        val textureIds = IntArray(n)
        GLES20.glGenTextures(n, textureIds, 0)
        return textureIds
    }

    fun genTexture() : Int{
        return genTextures(1)[0]
    }

    fun bindTextureToTextureUnit(
        textureId: Int,
        textureUnit: Int,
        textureType: Int = GLES20.GL_TEXTURE_2D
    ) {
        GLES20.glActiveTexture(textureUnit)
        GLES20.glBindTexture(textureType, textureId)
    }

    fun loadTextureFromFile(assetManager: AssetManager, texFile: String, textureUnit: Int): Int {
        val textureId = genTexture()
        if (textureId != 0) {
            bindTextureToTextureUnit(textureId, textureUnit)

            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )
            val options = BitmapFactory.Options().apply { inScaled = false }
            val inputStream = assetManager.open(texFile)
            val bmp = BitmapFactory.decodeStream(inputStream, null, options)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
            bmp?.recycle()
            // Now bind the texture ID to a texture unit as before, and send the texture unit to your shader...
        }
        return textureId
    }

}