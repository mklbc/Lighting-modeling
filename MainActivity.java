package com.example.kulubecioglu_lab42;


import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.opengl.GLSurfaceView;
import android.opengl.GLES32;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

// Uygulamanın ana aktivitesi: Menüden mod seçimi ve seçilen modu çalıştırma.
public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    private BaseRendererMode currentMode = null;  // Seçilen modun referansı

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new CustomGLSurfaceView(this);
        setContentView(glSurfaceView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Farklı sahne/aydınlatma modlarını menüye ekliyoruz.
        menu.add(0, 1, 0, "Diffuse Lighting");
        menu.add(0, 2, 0, "Specular Lighting");
        menu.add(0, 3, 0, "Rotating Pyramid");
        menu.add(0, 4, 0, "Nine Cubes");
        menu.add(0, 5, 0, "Cylinder");
        menu.add(0, 6, 0, "Torus");
        menu.add(0, 7, 0, "Shapes");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Seçilen menü öğesine göre ilgili modu başlat.
        setTitle(item.getTitle());
        switch (item.getItemId()) {
            case 1:
                startMode(new DiffuseLampPlaneMode(), GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                return true;
            case 2:
                startMode(new SpecularLampPlaneMode(), GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                return true;
            case 3:
                startMode(new RotatingPyramidMode(), GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                return true;
            case 4:
                startMode(new NineCubesRenderer(), GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                return true;
            case 5:
                startMode(new CustomCylinderMode(), GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                return true;
            case 6:
                startMode(new CustomTorusMode(), GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                return true;
            case 7:
                startMode(new ShapesRenderer(), GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Seçilen modu ve render modunu ayarlayıp yeniden çizim talep ediyoruz.
    private void startMode(BaseRendererMode mode, int renderMode) {
        currentMode = mode;
        glSurfaceView.setRenderMode(renderMode);
        glSurfaceView.requestRender();
    }

    // Özel GLSurfaceView sınıfı: Dokunma olaylarını yakalar ve modun ilgili metotlarını çağırır.
    private class CustomGLSurfaceView extends GLSurfaceView {
        public CustomGLSurfaceView(Context context) {
            super(context);
            // OpenGL ES 3.2 gibi bir sürüm hedeflenebilir
            setEGLContextClientVersion(3);
            setRenderer(new CustomGLRenderer());
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (currentMode == null) return false;
            if (currentMode.onTouchIgnored()) return false;

            int width = getWidth();
            int height = getHeight();
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (currentMode.onTouchDown(x, y, width, height)) {
                        requestRender();
                    }
                    setTitle(currentMode.getDebugInfo());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (currentMode.onTouchMove(x, y, width, height)) {
                        requestRender();
                    }
                    setTitle(currentMode.getDebugInfo());
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    // Renderer: Surface oluşturulduğunda, boyutu değiştiğinde ve her karede çizim yapılırken çağrılır.
    private class CustomGLRenderer implements GLSurfaceView.Renderer {
        private int renderWidth = 1, renderHeight = 1;

        @Override
        public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl,
                                     javax.microedition.khronos.egl.EGLConfig config) {
            GLES32.glEnable(GLES32.GL_DEPTH_TEST);
            GLES32.glClearColor(0f, 0f, 0.2f, 1f);
        }

        @Override
        public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
            GLES32.glViewport(0, 0, width, height);
            renderWidth = width;
            renderHeight = height;
        }

        @Override
        public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
            if (currentMode != null) {
                // Arka plan ve derinlik arabelleklerini temizle
                currentMode.clearScreenColor();
                GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

                // Shader programı oluşturulmamışsa oluştur
                if (currentMode.getShaderProgram() <= 0) {
                    currentMode.createShaderProgram();
                }
                // Programı kullan ve çizim yap
                GLES32.glUseProgram(currentMode.getShaderProgram());
                currentMode.useProgramForDrawing(renderWidth, renderHeight);
            }
        }
    }
}
