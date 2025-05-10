package boilerplate.utility;

public class Mat4f {
    float m00, m01, m02, m03;
    float m10, m11, m12, m13;
    float m20, m21, m22, m23;
    float m30, m31, m32, m33;

    public Mat4f perspective(float fovy, float aspect, float zNear, float zFar) {
        float h = (float) Math.tan(fovy * 0.5f);
        // calculate right matrix elements
        float rm00 = 1.0f / (h * aspect);
        float rm11 = 1.0f / h;
        float rm22;
        float rm32;
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            rm22 = e - 1.0f;
            rm32 = (e - 2.0f) * zNear;
        } else if (nearInf) {
            float e = 1E-6f;
            rm22 =  1.0f - e;
            rm32 = (2.0f - e) * zFar;
        } else {
            rm22 = (zFar + zNear) / (zNear - zFar);
            rm32 = (zFar + zFar) * zNear / (zNear - zFar);
        }
        // perform optimized matrix multiplication
        float nm20 = m20 * rm22 - m30;
        float nm21 = m21 * rm22 - m31;
        float nm22 = m22 * rm22 - m32;
        float nm23 = m23 * rm22 - m33;
        Mat4f m = new Mat4f();
        m.m00 = m00 * rm00;
        m.m01 = m01 * rm00;
        m.m02 = m02 * rm00;
        m.m03 = m03 * rm00;

        m.m10 = m10 * rm11;
        m.m11 = m11 * rm11;
        m.m12 = m12 * rm11;
        m.m13 = m13 * rm11;

        m.m30 = m20 * rm32;
        m.m31 = m21 * rm32;
        m.m32 = m22 * rm32;
        m.m33 = m23 * rm32;

        m.m20 = nm20;
        m.m21 = nm21;
        m.m22 = nm22;
        m.m23 = nm23;
        return m;
    }
}
