package freemap.openglwrapper

import android.opengl.Matrix

class GLMatrix {
    val values = FloatArray(16)

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
}