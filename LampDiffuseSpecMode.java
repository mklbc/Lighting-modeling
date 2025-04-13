package com.example.kulubecioglu_lab42;


import android.opengl.GLES32;
import android.opengl.Matrix;

// Diffuse + Specular aydınlatma hesaplamalarını içeren temel bir sınıf.
// BaseRendererMode'dan miras alır, ek olarak ışık ve obje rengi, ışık pozisyonu vb. içerir.
public class LampDiffuseSpecMode extends BaseRendererMode {
    protected float[] objectColor = {1f, 1f, 1f};
    protected float[] lightColor = {1f, 1f, 1f};
    protected float[] lightCoordinates = {0f, -1f, 1f};

    protected float touchXDown, touchYDown;
    protected float prevLightX, prevLightY;

    public LampDiffuseSpecMode() {
        super();
        angleAlpha = 0f;
        viewAngleBeta = 70f;
        initScene();  // Alt sınıfın sahne kurulum metodu
    }

    // Burada boş bıraktık, alt sınıflar kendine göre doldurabilir.
    @Override
    protected void initScene() {
        // Örnek olarak, geometry veya sahne verileri burada hazırlanabilir.
    }

    // Kamera ve projeksiyon matrislerini kurar.
    @Override
    protected void setupProjection(int width, int height) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setIdentityM(projMatrix, 0);

        // Ekranın ortasına bakacak şekilde kamerayı döndür ve konumlandır
        Matrix.rotateM(viewMatrix, 0, -viewAngleBeta, 1, 0, 0);
        Matrix.rotateM(viewMatrix, 0, -angleAlpha, 0, 0, 1);
        Matrix.translateM(viewMatrix, 0,
                -cameraPosition[0], -cameraPosition[1], -cameraPosition[2]);

        float aspect = (float) width / (float) height;
        Matrix.perspectiveM(projMatrix, 0, 35, aspect, 0.1f, 30f);

        // Shader'a matrisleri gönder
        int handle = GLES32.glGetUniformLocation(shaderProgram, "uViewMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, viewMatrix, 0);
        handle = GLES32.glGetUniformLocation(shaderProgram, "uProjMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, projMatrix, 0);
        handle = GLES32.glGetUniformLocation(shaderProgram, "uModelMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, modelMatrix, 0);

        // Işık ve obje renkleri
        handle = GLES32.glGetUniformLocation(shaderProgram, "vColorMode");
        GLES32.glUniform1i(handle, 1);
        handle = GLES32.glGetUniformLocation(shaderProgram, "vColor");
        GLES32.glUniform3fv(handle, 1, objectColor, 0);
        handle = GLES32.glGetUniformLocation(shaderProgram, "vLightColor");
        GLES32.glUniform3fv(handle, 1, lightColor, 0);
        handle = GLES32.glGetUniformLocation(shaderProgram, "vLightPos");
        GLES32.glUniform3fv(handle, 1, lightCoordinates, 0);
        handle = GLES32.glGetUniformLocation(shaderProgram, "vEyePos");
        GLES32.glUniform3fv(handle, 1, cameraPosition, 0);
    }

    // Sahne nesnelerini çizdikten sonra ışık kaynağını da çizmek istiyorsak
    // alt sınıflarda bu metodu çağırabiliriz.
    protected void drawLampSymbol() {
        if (primitiveObj == null) return; // Geometri nesnesi yoksa çık

        // Işık kaynağını göstermek için colorMode=0 ayarla
        int handle = GLES32.glGetUniformLocation(shaderProgram, "vColorMode");
        GLES32.glUniform1i(handle, 0);

        // Işık kaynağı model matrisini sıfırla ve ışığın konumuna taşı
        Matrix.setIdentityM(modelMatrix, 0);  // Model matrisini sıfırla
        Matrix.translateM(modelMatrix, 0, lightCoordinates[0], lightCoordinates[1], lightCoordinates[2]);

        // Shader’a güncellenmiş model matrisini gönder
        handle = GLES32.glGetUniformLocation(shaderProgram, "uModelMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, modelMatrix, 0);

        // İlk nesneyi (ışık kaynağı sembolü olan nokta) çiz
        primitiveObj.drawObjects(0, 0, -1);
    }



    @Override
    public boolean onTouchDown(float x, float y, int w, int h) {
        touchXDown = x;
        touchYDown = y;
        prevLightX = lightCoordinates[0];
        prevLightY = lightCoordinates[1];
        // Üst kısma dokunulursa ışığı yukarı çek
        if (y < 0.1f * h) {
            lightCoordinates[2] += 0.1f;
            return true;
        }
        // Alt kısma dokunulursa ışığı aşağı çek
        if (y > 0.9f * h) {
            lightCoordinates[2] -= 0.1f;
        }
        return true;
    }

    @Override
    public boolean onTouchMove(float x, float y, int w, int h) {
        // Dokunma hareketine göre ışık konumunu güncelle
        lightCoordinates[0] = prevLightX + 0.005f * (x - touchXDown);
        lightCoordinates[1] = prevLightY - 0.005f * (y - touchYDown);
        return true;
    }

    @Override
    public String getDebugInfo() {
        return String.format("x=%.1f y=%.1f z=%.1f",
                lightCoordinates[0], lightCoordinates[1], lightCoordinates[2]);
    }
}
