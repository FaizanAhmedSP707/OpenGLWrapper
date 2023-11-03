package freemap.openglwrapper

import android.opengl.Matrix

class GLMatrix {
    val values = FloatArray(16)
    val axes = mapOf('x' to floatArrayOf(1.0f, 0.0f, 0.0f), 'y' to floatArrayOf(0.0f, 1.0f, 0.0f), 'z' to floatArrayOf(0.0f, 0.0f, 1.0f))

    init {
        setAsIdentityMatrix()
    }

    fun setAsIdentityMatrix() {
        Matrix.setIdentityM(values, 0)
    }

    fun setProjectionMatrix(hFov: Float, aspect: Float, near: Float, far: Float) {
        Matrix.perspectiveM(values, 0, hFov/aspect, aspect, near, far)
    }

    fun translate(dx: Float, dy: Float, dz: Float) {
        Matrix.translateM(values, 0, dx, dy, dz)
    }

    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.rotateM(values, 0, angle, x, y, z)
    }

    fun rotateAboutAxis(angle: Float, axis: Char) : Boolean {
        axes[axis]?.apply {
            rotate(angle, this[0], this[1], this[2])
            return true
        }
        return false
    }

    fun rotateAboutY(angle: Float) {
        rotate(angle, 1.0f, 0.0f, 0.0f)
    }
}