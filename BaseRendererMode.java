package com.example.kulubecioglu_lab42;


import android.opengl.GLES32;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

// Tüm örneklerin temel sınıfı. Shader derleme/linkleme, VAO/VBO yönetimi ve dokunmatik olayları içerir.
// Alt sınıflar bu sınıfı genişleterek kendi sahne, aydınlatma ve geometri işlevlerini ekleyebilir.
public class BaseRendererMode {
    protected int shaderProgram = 0;
    protected int vaoId = 0;
    protected int vboId = 0;

    // Sahne geometrisi için yardımcı sınıf
    protected GraphicsPrimitives primitiveObj = null;
    // Vertex dizisi (pozisyon + normal gibi veriler)
    protected float[] sceneVertices = null;
    protected int vertexCount = 0;

    // Kamera konumu ve açıları
    protected float[] cameraPosition = {0f, -4f, 1.5f};
    protected float angleAlpha = 0f;      // Yatay dönüş açısı (örn. Z ekseni etrafı)
    protected float viewAngleBeta = 0f;   // Dikey dönüş açısı (örn. X ekseni etrafı)

    // Alt sınıflarda kullanılacak matrisler
    protected float[] modelMatrix;
    protected float[] viewMatrix;
    protected float[] projMatrix;

    public BaseRendererMode() {
        modelMatrix = new float[16];
        viewMatrix = new float[16];
        projMatrix = new float[16];
    }

    // Ekran temizleme rengi (alt sınıflar override edebilir)
    public void clearScreenColor() {
        // Varsayılan: siyah (0,0,0,1). Alt sınıf değiştirirse override
        GLES32.glClearColor(0f, 0f, 0f, 1f);
    }

    // Alt sınıfların sahne oluşturma işlemlerini yapacağı metot (boş).
    protected void initScene() { }

    // Shader programı oluşturma (alt sınıflar override edebilir)
    public void createShaderProgram() { }

    // Projeksiyon matrislerini ayarlayan metot (boş, alt sınıflar override edebilir)
    protected void setupProjection(int width, int height) { }

    // Tüm sahneyi çizen metot (boş, alt sınıflar override edebilir)
    protected void drawScene() { }

    // Sahnede spesifik nesneleri çizecek metot (boş, alt sınıflar override edebilir)
    protected void drawSceneObjects() { }

    // GPU’da derlenecek shader kodunu derler ve döndürür
    protected int compileShader(int type, String shaderCode) {
        int shaderId = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shaderId, shaderCode);
        GLES32.glCompileShader(shaderId);
        int[] status = new int[1];
        GLES32.glGetShaderiv(shaderId, GLES32.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            // Hata varsa log alabilirsiniz: GLES32.glGetShaderInfoLog(shaderId)
            return 0;
        }
        return shaderId;
    }

    // Vertex ve fragment shader’ları derleyip programa ekleyip linkler.
    protected void compileAndAttachShaders(String vertexCode, String fragmentCode) {
        shaderProgram = -1; // Başlangıçta hatalı say
        int vsId = compileShader(GLES32.GL_VERTEX_SHADER, vertexCode);
        if (vsId == 0) return;
        int fsId = compileShader(GLES32.GL_FRAGMENT_SHADER, fragmentCode);
        if (fsId == 0) return;

        shaderProgram = GLES32.glCreateProgram();
        GLES32.glAttachShader(shaderProgram, vsId);
        GLES32.glAttachShader(shaderProgram, fsId);
        GLES32.glLinkProgram(shaderProgram);

        // Artık shader kaynakları silinebilir
        GLES32.glDeleteShader(vsId);
        GLES32.glDeleteShader(fsId);
    }

    // VAO ve VBO için ID oluşturur.
    protected void initializeVAOVBO() {
        int[] tmp = new int[2];
        GLES32.glGenVertexArrays(1, tmp, 0);
        vaoId = tmp[0];
        GLES32.glGenBuffers(1, tmp, 0);
        vboId = tmp[0];
    }

    // Tekli öznitelik (örneğin sadece pozisyon) içeren vertex dizisini bağlar.
    protected void bindVertexArraySingle(float[] sourceData, String attribName) {
        if (shaderProgram <= 0) return;
        ByteBuffer bb = ByteBuffer.allocateDirect(sourceData.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(sourceData);
        fb.position(0);

        initializeVAOVBO();
        GLES32.glBindVertexArray(vaoId);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboId);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sourceData.length * 4,
                fb, GLES32.GL_STATIC_DRAW);

        int handle = GLES32.glGetAttribLocation(shaderProgram, attribName);
        GLES32.glEnableVertexAttribArray(handle);
        GLES32.glVertexAttribPointer(handle, 3, GLES32.GL_FLOAT, false, 3 * 4, 0);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);
    }

    // İkili öznitelik (örneğin pozisyon + normal) içeren vertex dizisini bağlar.
    protected void bindVertexArrayDouble(float[] sourceData, int stride,
                                         String attrib1, int offset1,
                                         String attrib2, int offset2) {
        if (shaderProgram <= 0) return;
        ByteBuffer bb = ByteBuffer.allocateDirect(sourceData.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(sourceData);
        fb.position(0);

        initializeVAOVBO();
        GLES32.glBindVertexArray(vaoId);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboId);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sourceData.length * 4,
                fb, GLES32.GL_STATIC_DRAW);

        int handle = GLES32.glGetAttribLocation(shaderProgram, attrib1);
        GLES32.glEnableVertexAttribArray(handle);
        GLES32.glVertexAttribPointer(handle, 3, GLES32.GL_FLOAT, false,
                stride * 4, offset1);

        handle = GLES32.glGetAttribLocation(shaderProgram, attrib2);
        GLES32.glEnableVertexAttribArray(handle);
        GLES32.glVertexAttribPointer(handle, 3, GLES32.GL_FLOAT, false,
                stride * 4, offset2);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);
    }

    // Çizim döngüsünde her kare çağrılır.
    // Alt sınıflar bu metodu genelde override edip kendi matrislerini ve çizimlerini ekler.
    public void useProgramForDrawing(int width, int height) {
        setupProjection(width, height);
        drawScene();
    }

    // Dokunmatik olaylarının varsayılan (boş) implementasyonları
    public boolean onTouchIgnored() { return false; }
    public boolean onTouchDown(float x, float y, int screenWidth, int screenHeight) { return false; }
    public boolean onTouchMove(float x, float y, int screenWidth, int screenHeight) { return false; }

    // Ekran üzerinde bilgi göstermek için
    public String getDebugInfo() {
        return String.format("Alpha=%.1f Beta=%.1f", angleAlpha, viewAngleBeta);
    }

    // Açıları normalize eder (opsiyonel)
    public void normalizeAngles() {
        if (angleAlpha >= 360) angleAlpha -= 360;
        if (angleAlpha <= -360) angleAlpha += 360;
        if (viewAngleBeta > 90f) viewAngleBeta = 90f;
        if (viewAngleBeta < -90f) viewAngleBeta = -90f;
    }

    // getShaderProgram() ile dışarıdan program ID'sini öğrenebiliriz.
    public int getShaderProgram() {
        return shaderProgram;
    }
}
