package freemap.openglwrapper

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log

import java.nio.Buffer


// Controls the interface between CPU and GPU, i.e. all the interfacing with shaders
// and associating buffer data with shader variables.

// Code taken from Hikar
//
// This code is based on the GPUInterface class from Hikar (https://gitlab.com/nickw1/Hikar), with
// modifications.
//
// GPUInterface was originally licensed under the GPL. As the author of GPUInterface, however,
// I am re-licensing THIS version (GLWrapper) under the Lesser GNU General Public License, v3,
// to permit its use in non-free programs.
// The original GPUInterface from Hikar remains, of course, under the GPL.


class GPUInterface(val id: String="DefaultGPUInterface")  {

    var shaderProgram = -1
        private set
    var lastShaderError = ""
        private set

    fun loadShaders(am: AssetManager, vertexShaderFile: String, fragmentShaderFile: String): Boolean {
        val vertexShaderSrc = OpenGLUtils.loadShader(am, vertexShaderFile)
        val fragmentShaderSrc = OpenGLUtils.loadShader(am, fragmentShaderFile)
        Log.d("OpenGLWrapper.GPUInterface", vertexShaderSrc)
        Log.d("OpenGLWrapper.GPUInterface", fragmentShaderSrc)
        return compileAndLinkShaders(vertexShaderSrc, fragmentShaderSrc)
    }

    fun compileAndLinkShaders(vertexShaderCode: String, fragmentShaderCode: String) : Boolean {
        val vertexShader = addVertexShader(vertexShaderCode)

        if (vertexShader >= 0) {
            val fragmentShader = addFragmentShader(fragmentShaderCode)
            if (fragmentShader >= 0) {
                shaderProgram = makeProgram(vertexShader, fragmentShader)
                return true
            }
        }
        return false
    }

    // must call to use a particular shader program
    fun select() {
        GLES20.glUseProgram(shaderProgram)
    }


    fun drawBufferedTriangles(firstVertex: Int, nVertices: Int) {
        if (valid) {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, firstVertex, nVertices)
        }
    }

    // Draw buffered data:
    // we select the buffer, get the shader var ref, tell opengl the format of the data
    // and then draw the data
    fun drawIndexedBufferedData(vertices: Buffer, indices: Buffer, stride: Int, attrVarRef: Int, startOfIndicesInBuffer: Int  = 0) {
        drawIndexedBufferedData(vertices, indices, stride, attrVarRef, GLES20.GL_TRIANGLES, startOfIndicesInBuffer)
    }

    fun drawIndexedBufferedData(vertices: Buffer, indices: Buffer,  stride: Int, attrVarRef: Int, mode: Int, startOfIndicesInBuffer: Int = 0) {
        if (valid) {
            specifyBufferedDataFormat(attrVarRef, vertices, stride)
            indices.position(startOfIndicesInBuffer)
            drawElements(indices, mode)
        }
    }

    fun specifyBufferedDataFormat(attrVarRef: Int, vertices: Buffer, stride: Int, startOfDataInBuffer: Int = 0, valuesPerVertex: Int = 3) {
        vertices.position(startOfDataInBuffer)
        GLES20.glEnableVertexAttribArray(attrVarRef)
        GLES20.glVertexAttribPointer(attrVarRef, valuesPerVertex, GLES20.GL_FLOAT, false, stride, vertices)
    }

    fun drawElements(indices: Buffer, mode: Int = GLES20.GL_TRIANGLES) {
        GLES20.glDrawElements(mode, indices.limit(), GLES20.GL_UNSIGNED_SHORT, indices)
    }
    fun getAttribLocation(shaderVar: String): Int {
        return if (valid) GLES20.glGetAttribLocation(shaderProgram, shaderVar) else -1
    }

    fun getUniformLocation(shaderVar: String) : Int {
        return if (valid) GLES20.glGetUniformLocation(shaderProgram, shaderVar) else -1
    }

    // Do something with the view and perspective matrices

    fun sendMatrix(refMtxVar: Int, mtx: GLMatrix) {
        if (valid) {
            //    Log.d("hikar", "for GPUInterface " + wayId + " and shaderProgram " + shaderProgram + ": sendMatrix(): refMtxVar=" + refMtxVar + " for shader variable: " + shaderMtxVar);
            errorCheck("glGetUniformLocation")
            GLES20.glUniformMatrix4fv(refMtxVar, 1, false, mtx.values, 0) // 1 = one matrix http://www.khronos.org/opengles/sdk/docs/man/xhtml/glUniform.xml
            errorCheck("sending over matrix")
        }
    }

    // could be used e.g. for sending colours
    fun setUniform4FloatArray(refShaderVar: Int, value: FloatArray) {
        if (valid) {
            GLES20.glUniform4fv(refShaderVar, 1, value, 0) // 1 = one uniform variable http://www.khronos.org/opengles/sdk/docs/man/xhtml/glUniform.xml
        }
    }

    // could be used e.g. for sending texture id
    fun setUniformInt(refShaderVar: Int, i: Int) {
        if (valid) {
            GLES20.glUniform1i(refShaderVar, i)
        }
    }

    public val valid: Boolean
        get() = shaderProgram >= 0



    fun drawTexturedBufferedData(vertices: Buffer, indices: Buffer, attrVarRef: Int, attrTexVarRef: Int, mode: Int) {
        if (valid) {
            errorCheck("getShaderVarRef, vertices, attrVarRef=$attrVarRef")
            GLES20.glEnableVertexAttribArray(attrVarRef)
            errorCheck("glEnableVertexAttribArray, vertices")


            vertices.position(0)
            GLES20.glVertexAttribPointer(attrVarRef, 3, GLES20.GL_FLOAT, false, 20, vertices)
            errorCheck("glVertexAttribPointer, vertices")

            errorCheck("getShaderVarRef, tex")
            GLES20.glEnableVertexAttribArray(attrTexVarRef)
            errorCheck("glEnableVertexAttribArray, tex")
            vertices.position(3)
            GLES20.glVertexAttribPointer(attrTexVarRef, 2, GLES20.GL_FLOAT, false, 20, vertices)
            errorCheck("glVertexAttribPointer, tex")

            indices.position(0)
            //   vertices.position(0);
            GLES20.glDrawElements(mode, indices.limit(), GLES20.GL_UNSIGNED_SHORT, indices)
            errorCheck("glDrawElements")

        }
    }

    fun addVertexShader(shaderCode: String): Int {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode)
    }

    fun addFragmentShader(shaderCode: String): Int {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode)
    }

    fun compileShader(shaderType: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(shaderType)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

        if (compileStatus[0] == 0) {
            lastShaderError = GLES20.glGetProgramInfoLog(shader)
            GLES20.glDeleteShader(shader)
            return -1
        }
        return shader
    }

    fun makeProgram(vertexShader: Int, fragmentShader: Int): Int {
        val shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == 0) {
            lastShaderError = GLES20.glGetProgramInfoLog(shaderProgram)
            GLES20.glDeleteProgram(shaderProgram)
            return -1
        }
        GLES20.glUseProgram(shaderProgram)
        return shaderProgram
    }


    private fun errorCheck(location: String) {
        val i = GLES20.glGetError()
        if(i != GLES20.GL_NO_ERROR) {
            Log.e("OpenGLWrapper.GPUInterface", "**********OpenGL error for GPU interface $id at $location code $i")
        }
    }
}