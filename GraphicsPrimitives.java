package com.example.kulubecioglu_lab42;


import android.opengl.GLES32;

// Çeşitli geometri (üçgen, dörtgen, küp, küre, silindir, torus, satranç tahtası, piramit vb.)
// eklemek için yardımcı metotları barındıran sınıf.
public class GraphicsPrimitives {
    private int objectCount;
    private int capacity;

    private int[] startIndices;
    private int[] drawModes;
    private float[] colorData;

    public GraphicsPrimitives() {
        objectCount = 0;
        capacity = 100;
        startIndices = new int[capacity];
        drawModes = new int[capacity];
        colorData = new float[capacity * 3];
    }

    private void expandArrays(int newCap) {
        if (newCap <= capacity) return;
        int[] newStarts = new int[newCap];
        int[] newModes = new int[newCap];
        float[] newColors = new float[newCap * 3];

        System.arraycopy(startIndices, 0, newStarts, 0, capacity);
        System.arraycopy(drawModes, 0, newModes, 0, capacity);
        System.arraycopy(colorData, 0, newColors, 0, capacity * 3);

        startIndices = newStarts;
        drawModes = newModes;
        colorData = newColors;
        capacity = newCap;
    }

    // Yeni nesnenin başlangıç indexini kaydeder
    public int startNewObject(int pos, int glMode, float r, float g, float b) {
        int startV = pos / 6; // her vertex 6 float: (xyz + normal)
        if (objectCount >= capacity - 2) {
            expandArrays(capacity + 100);
        }
        startIndices[objectCount] = startV;
        drawModes[objectCount] = glMode;

        int colorIdx = objectCount * 3;
        colorData[colorIdx++] = r;
        colorData[colorIdx++] = g;
        colorData[colorIdx] = b;

        objectCount++;
        startIndices[objectCount] = startV;
        return startV;
    }

    // Nesnenin bitiş indexini kaydeder
    public void endObject(int pos) {
        startIndices[objectCount] = pos / 6;
    }

    public int getObjectCount() {
        return objectCount;
    }

    // Nesneleri çizme döngüsü
    public void drawObjects(int startObj, int endObj, int colorHandle) {
        if (startObj < 0) startObj = 0;
        if (endObj >= objectCount) endObj = objectCount - 1;
        if (startObj > endObj) return;

        for (int i = startObj; i <= endObj; i++) {
            int startV = startIndices[i];
            int countV = startIndices[i + 1] - startV;
            if (countV <= 0) continue;

            if (colorHandle >= 0) {
                int idx = i * 3;
                float[] clr = {
                        colorData[idx], colorData[idx + 1], colorData[idx + 2]
                };
                GLES32.glUniform3fv(colorHandle, 1, clr, 0);
            }
            GLES32.glDrawArrays(drawModes[i], startV, countV);
        }
    }

    // Tek bir vertex (xyz + normal) ekler
    public int addVertexWithNormal(float[] arr, int pos,
                                   float x, float y, float z,
                                   float nx, float ny, float nz) {
        if (arr == null) return pos + 6;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < 1e-8) length = 1f; // 0'dan kaçın
        arr[pos++] = x;
        arr[pos++] = y;
        arr[pos++] = z;
        arr[pos++] = nx / length;
        arr[pos++] = ny / length;
        arr[pos++] = nz / length;
        return pos;
    }

    // Nokta ekler (ışık kaynağı sembolü gibi)
    public int addPointWithNormal(float[] arr, int pos,
                                  float x, float y, float z,
                                  float r, float g, float b) {
        int start = startNewObject(pos, GLES32.GL_POINTS, r, g, b);
        pos = addVertexWithNormal(arr, pos, x, y, z, 0, 0, 1);
        endObject(pos);
        return pos;
    }

    // Üçgen ekleme
    public int addTriangleWithNormal(float[] arr, int pos,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float x3, float y3, float z3,
                                     float r, float g, float b) {
        if (arr == null) return pos + 18;
        startNewObject(pos, GLES32.GL_TRIANGLES, r, g, b);

        float xn = (z2 - z1) * (y3 - y1) - (y2 - y1) * (z3 - z1);
        float yn = (x2 - x1) * (z3 - z1) - (z2 - z1) * (x3 - x1);
        float zn = (y2 - y1) * (x3 - x1) - (x2 - x1) * (y3 - y1);

        pos = addVertexWithNormal(arr, pos, x1, y1, z1, xn, yn, zn);
        pos = addVertexWithNormal(arr, pos, x2, y2, z2, xn, yn, zn);
        pos = addVertexWithNormal(arr, pos, x3, y3, z3, xn, yn, zn);

        endObject(pos);
        return pos;
    }

    // Dörtgen (quad) ekleme (TRIANGLE_FAN ile)
    public int addQuadWithNormal(float[] arr, int pos,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 float x4, float y4, float z4,
                                 float r, float g, float b) {
        if (arr == null) return pos + 6 * 4;  // yani pos + 24

        startNewObject(pos, GLES32.GL_TRIANGLE_FAN, r, g, b);

        float xn = (z2 - z1) * (y3 - y1) - (y2 - y1) * (z3 - z1);
        float yn = (x2 - x1) * (z3 - z1) - (z2 - z1) * (x3 - x1);
        float zn = (y2 - y1) * (x3 - x1) - (x2 - x1) * (y3 - y1);

        pos = addVertexWithNormal(arr, pos, x1, y1, z1, xn, yn, zn);
        pos = addVertexWithNormal(arr, pos, x2, y2, z2, xn, yn, zn);
        pos = addVertexWithNormal(arr, pos, x3, y3, z3, xn, yn, zn);
        pos = addVertexWithNormal(arr, pos, x4, y4, z4, xn, yn, zn);

        endObject(pos);
        return pos;
    }

    // Piramit ekleme
    public int addPyramidWithNormal(float[] arr, int pos,
                                    float cx, float cy, float cz,
                                    float size,
                                    float r, float g, float b) {
        if (arr == null) return pos;
        float half = 0.5f * size;
        // Ön yüz
        pos = addTriangleWithNormal(arr, pos,
                cx - half, cy - half, cz,
                cx,        cy,        cz + size,
                cx + half, cy - half, cz,
                r, g, b);
        // Sağ yüz
        pos = addTriangleWithNormal(arr, pos,
                cx + half, cy - half, cz,
                cx,        cy,        cz + size,
                cx + half, cy + half, cz,
                r, g, b);
        // Sol yüz
        pos = addTriangleWithNormal(arr, pos,
                cx - half, cy + half, cz,
                cx,        cy,        cz + size,
                cx - half, cy - half, cz,
                r, g, b);
        // Arka yüz
        pos = addTriangleWithNormal(arr, pos,
                cx + half, cy + half, cz,
                cx,        cy,        cz + size,
                cx - half, cy + half, cz,
                r, g, b);
        return pos;
    }

    // Küp ekleme
    public int addCubeWithNormal(float[] arr, int pos,
                                 float cx, float cy, float cz,
                                 float size,
                                 float r, float g, float b) {
        if (arr == null) return pos;
        float half = 0.5f * size;

        // Ön yüz
        pos = addQuadWithNormal(arr, pos,
                cx - half, cy - half, cz - half,
                cx - half, cy - half, cz + half,
                cx + half, cy - half, cz + half,
                cx + half, cy - half, cz - half,
                r, g, b);
        // Arka yüz
        pos = addQuadWithNormal(arr, pos,
                cx - half, cy + half, cz - half,
                cx + half, cy + half, cz - half,
                cx + half, cy + half, cz + half,
                cx - half, cy + half, cz + half,
                r, g, b);
        // Sol yüz
        pos = addQuadWithNormal(arr, pos,
                cx - half, cy - half, cz - half,
                cx - half, cy + half, cz - half,
                cx - half, cy + half, cz + half,
                cx - half, cy - half, cz + half,
                r, g, b);
        // Sağ yüz
        pos = addQuadWithNormal(arr, pos,
                cx + half, cy - half, cz - half,
                cx + half, cy - half, cz + half,
                cx + half, cy + half, cz + half,
                cx + half, cy + half, cz - half,
                r, g, b);
        // Alt yüz
        pos = addQuadWithNormal(arr, pos,
                cx - half, cy - half, cz - half,
                cx + half, cy - half, cz - half,
                cx + half, cy + half, cz - half,
                cx - half, cy + half, cz - half,
                r, g, b);
        // Üst yüz
        pos = addQuadWithNormal(arr, pos,
                cx - half, cy - half, cz + half,
                cx - half, cy + half, cz + half,
                cx + half, cy + half, cz + half,
                cx + half, cy - half, cz + half,
                r, g, b);

        return pos;
    }

    // Satranç tahtası ekleme
    public int addChessBoardWithNormal(float[] arr, int pos,
                                       int lines, float cellSize,
                                       float cx, float cy, float cz,
                                       float r1, float g1, float b1,
                                       float r2, float g2, float b2) {
        if (arr == null) return pos + lines * lines * 24;



        float shift = 0.5f * lines * cellSize;
        for (int yy = 0; yy < lines; yy++) {
            float y = cy + yy * cellSize - shift;
            for (int xx = 0; xx < lines; xx++) {
                float x = cx + xx * cellSize - shift;
                float rr, gg, bb;
                if (((xx + yy) & 1) == 0) {
                    rr = r1; gg = g1; bb = b1;
                } else {
                    rr = r2; gg = g2; bb = b2;
                }
                pos = addQuadWithNormal(arr, pos,
                        x,       y,       cz,
                        x,       y + cellSize, cz,
                        x + cellSize, y + cellSize, cz,
                        x + cellSize, y, cz,
                        rr, gg, bb);
            }
        }
        return pos;
    }

    // Silindir ekleme
    public int addCylinderWithNormal(float[] arr, int pos,
                                     float cx, float cy, float cz,
                                     float radius, float height, int seg,
                                     float r, float g, float b,
                                     float normalDir) {
        // Eğer dizi henüz oluşturulmamışsa, ekleyeceğimiz toplam float sayısını hesapla.
        if (arr == null) return pos + 6 * (2 * seg + 2);

        float half = 0.5f * height;

        startNewObject(pos, GLES32.GL_TRIANGLE_STRIP, r, g, b);

        float startNX = 1f, startNY = 0f;
        float startX = cx + radius, startY = cy;

        pos = addVertexWithNormal(arr, pos,
                startX, startY, cz - half,
                normalDir * startNX, normalDir * startNY, 0f);
        pos = addVertexWithNormal(arr, pos,
                startX, startY, cz + half,
                normalDir * startNX, normalDir * startNY, 0f);

        for (int i = 1; i < seg; i++) {
            float angle = (float) (2.0 * Math.PI * i / seg);
            float nx = (float) Math.cos(angle);
            float ny = (float) Math.sin(angle);
            float x = cx + radius * nx;
            float y = cy + radius * ny;

            pos = addVertexWithNormal(arr, pos,
                    x, y, cz - half,
                    normalDir * nx, normalDir * ny, 0f);
            pos = addVertexWithNormal(arr, pos,
                    x, y, cz + half,
                    normalDir * nx, normalDir * ny, 0f);
        }

        // Kapatma: İlk eklenen vertex'leri tekrarla.
        pos = addVertexWithNormal(arr, pos,
                startX, startY, cz - half,
                normalDir * startNX, normalDir * startNY, 0f);
        pos = addVertexWithNormal(arr, pos,
                startX, startY, cz + half,
                normalDir * startNX, normalDir * startNY, 0f);

        endObject(pos);
        return pos;
    }


    // Küre ekleme
    public int addSphereWithNormal(float[] arr, int pos,
                                   float cx, float cy, float cz,
                                   float radius, int segLat, int segLong,
                                   float r, float g, float b) {
        if (arr == null) return pos + 6 * (2 * (segLong + 2) + 2 * (segLong + 1) * (segLat - 2));

        if (segLat < 2 || segLong < 2) return pos;

        // Kuzey kutbu
        startNewObject(pos, GLES32.GL_TRIANGLE_FAN, r, g, b);
        pos = addVertexWithNormal(arr, pos,
                cx, cy, cz + radius,
                0, 0, 1);
        double theta = Math.PI * (0.5 - 1.0 / segLat);
        for (int i = 0; i <= segLong; i++) {
            double phi = 2.0 * Math.PI * i / segLong;
            float nx = (float) (Math.cos(theta) * Math.cos(phi));
            float ny = (float) (Math.cos(theta) * Math.sin(phi));
            float nz = (float) Math.sin(theta);
            pos = addVertexWithNormal(arr, pos,
                    cx + nx * radius, cy + ny * radius, cz + nz * radius,
                    nx, ny, nz);
        }
        endObject(pos);

        // Güney kutbu
        startNewObject(pos, GLES32.GL_TRIANGLE_FAN, r, g, b);
        pos = addVertexWithNormal(arr, pos,
                cx, cy, cz - radius,
                0, 0, -1);
        theta = -Math.PI * (0.5 - 1.0 / segLat);
        for (int i = 0; i <= segLong; i++) {
            double phi = 2.0 * Math.PI * i / segLong;
            float nx = (float) (Math.cos(theta) * Math.cos(phi));
            float ny = (float) (Math.cos(theta) * Math.sin(phi));
            float nz = (float) Math.sin(theta);
            pos = addVertexWithNormal(arr, pos,
                    cx + nx * radius, cy + ny * radius, cz + nz * radius,
                    nx, ny, nz);
        }
        endObject(pos);

        // Orta enlemler
        for (int lat = 1; lat < segLat - 1; lat++) {
            startNewObject(pos, GLES32.GL_TRIANGLE_STRIP, r, g, b);
            for (int lon = 0; lon <= segLong; lon++) {
                double phi = 2.0 * Math.PI * lon / segLong;
                theta = -Math.PI * (0.5 - (double) lat / segLat);
                float nx = (float) (Math.cos(theta) * Math.cos(phi));
                float ny = (float) (Math.cos(theta) * Math.sin(phi));
                float nz = (float) Math.sin(theta);
                pos = addVertexWithNormal(arr, pos,
                        cx + nx * radius, cy + ny * radius, cz + nz * radius,
                        nx, ny, nz);

                theta = -Math.PI * (0.5 - (double) (lat + 1) / segLat);
                nx = (float) (Math.cos(theta) * Math.cos(phi));
                ny = (float) (Math.cos(theta) * Math.sin(phi));
                nz = (float) Math.sin(theta);
                pos = addVertexWithNormal(arr, pos,
                        cx + nx * radius, cy + ny * radius, cz + nz * radius,
                        nx, ny, nz);
            }
            endObject(pos);
        }
        return pos;
    }

    // Torus ekleme
    public int addTorusWithNormal(float[] arr, int pos,
                                  float cx, float cy, float cz,
                                  float majorRadius, float minorRadius,
                                  int segLat, int segLong,
                                  float r, float g, float b) {
        if (arr == null) return pos + 12 * segLat * (segLong + 1);

        if (segLat < 2 || segLong < 2) return pos;

        for (int i = 0; i < segLat; i++) {
            startNewObject(pos, GLES32.GL_TRIANGLE_STRIP, r, g, b);
            for (int j = 0; j <= segLong; j++) {
                float phi = (float) (2.0 * Math.PI * j / segLong);
                float xBase = majorRadius * (float) Math.cos(phi);
                float yBase = majorRadius * (float) Math.sin(phi);

                float alpha = (float) (2.0 * Math.PI * i / segLat);
                float nx = (float) (Math.cos(alpha) * Math.cos(phi));
                float ny = (float) (Math.cos(alpha) * Math.sin(phi));
                float nz = (float) Math.sin(alpha);

                float px = cx + xBase + minorRadius * nx;
                float py = cy + yBase + minorRadius * ny;
                float pz = cz + minorRadius * nz;

                pos = addVertexWithNormal(arr, pos,
                        px, py, pz,
                        nx, ny, nz);

                alpha = (float) (2.0 * Math.PI * (i + 1) / segLat);
                nx = (float) (Math.cos(alpha) * Math.cos(phi));
                ny = (float) (Math.cos(alpha) * Math.sin(phi));
                nz = (float) Math.sin(alpha);

                px = cx + xBase + minorRadius * nx;
                py = cy + yBase + minorRadius * ny;
                pz = cz + minorRadius * nz;

                pos = addVertexWithNormal(arr, pos,
                        px, py, pz,
                        nx, ny, nz);
            }
            endObject(pos);
        }
        return pos;
    }
}
