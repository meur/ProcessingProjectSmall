import processing.core.PApplet;
import processing.core.PShape;
import processing.data.Table;
import processing.data.TableRow;

import java.util.HashSet;
import java.util.Set;

public class Application extends PApplet {

    PShape hungary;
    Table table;
    TableRow lakossag;

    int mapWidth;
    int mapHeight;
    int mapLeftMargin;
    int mapTopMargin;

    float maxRatio = 0;
    float minRatio = 1;
    int maxAbs = 0;
    int minAbs = 10000000;

    final int rollbarHeight = 30;
    final float miniboxW = 8;
    final float miniboxH = 20;
    float rollbarCenterY;
    float selectedMin = 0;
    float selectedMax = 1;
    boolean mouseOverMin;
    boolean mouseOverMax;
    boolean mouseLockedMax = false;
    boolean mouseLockedMin = false;
    boolean minActive = false;
    boolean maxActive = false;

    final int MAX_LIGHT = 240;

    final int chartTopMargin = 30;
    final int chartLeftMargin = chartTopMargin;
    final Set<String> selectedCounties = new HashSet<String>();

    @Override
    public void settings() {
        size(1356, 710);
    }

    @Override
    public void setup() {
        noStroke();
        frameRate(30);
        hungary = loadShape("HU_counties_blank.svg");
        table = loadTable("data.csv", "header");

        lakossag = table.findRow("lakossag", "date");

        selectedCounties.add("hb");

        setupShapeSize();
        setupActiveBar();

        rollbarCenterY = height - rollbarHeight / 2;
        noLoop();
    }

    void setupActiveBar() {
        maxActive = (mouseOverMax || mouseLockedMax) && !mouseLockedMin;
        minActive = (mouseOverMin || mouseLockedMin) && !mouseLockedMax;
    }

    void setupShapeSize() {
        float shapeWidth = hungary.width;
        float shapeHeight = hungary.height;

        final int maxWidth = width / 2;
        if (shapeWidth > maxWidth) {
            final float resizeRatio = maxWidth / shapeWidth;
            shapeWidth = maxWidth;
            shapeHeight *= resizeRatio;
        }

        final int maxHeight = (height - rollbarHeight) / 2;
        if (shapeHeight > maxHeight) {
            final float resizeRatio = maxHeight / shapeHeight;
            shapeHeight = maxHeight;
            shapeWidth *= resizeRatio;
        }

        mapWidth = (int) shapeWidth;
        mapHeight = (int) shapeHeight;
        mapLeftMargin = (maxWidth - mapWidth) / 2;
        mapTopMargin = (maxHeight - mapHeight) / 2;
    }

    @Override
    public void draw() {
        background(255);

        mouseOverMax = (Math.abs(rollbarCenterY - mouseY) < miniboxH / 2 + 2) && (Math.abs(map(selectedMax, 0, 1, mapLeftMargin, mapLeftMargin + mapWidth) - mouseX) < miniboxW / 2 + 2);
        mouseOverMin = !mouseOverMax &&
                (Math.abs(rollbarCenterY - mouseY) < miniboxH / 2 + 2) && (Math.abs(map(selectedMin, 0, 1, mapLeftMargin, mapLeftMargin + mapWidth) - mouseX) < miniboxW / 2 + 2);

        setupRollbar();

        TableRow row = getSelectedRow();

        maxRatio = 0;
        minRatio = 1;
        maxAbs = 0;
        minAbs = 10000000;
        for (int m = 1; m < row.getColumnCount() - 1; m++) {
            final String countieName = row.getColumnTitle(m);

            final int lak = lakossag.getInt(countieName);
            final int cov = row.getInt(countieName);
            float arany = (float)cov / lak;
            if (arany > maxRatio) {
                maxRatio = arany;
            }
            if (arany < minRatio) {
                minRatio = arany;
            }
            if (cov > maxAbs) {
                maxAbs = cov;
            }
            if (cov < minAbs) {
                minAbs = cov;
            }
        }

        drawSamples();

        for (int m = 1; m < row.getColumnCount() - 1; m++) {
            final String countieName = row.getColumnTitle(m);

            final int lak = lakossag.getInt(countieName);
            final int cov = row.getInt(countieName);
            float arany = ((float)cov / lak);
            final int colour0 = (int)map(arany, minRatio, maxRatio, MAX_LIGHT, 0);
            final int colour1 = (int)map(cov, minAbs, maxAbs, MAX_LIGHT, 0);

            final PShape countie = hungary.getChild(countieName);
            countie.disableStyle();

            fill(colour0);
            shapeTop(countie);
            fill(colour1);
            shapeBottom(countie);
        }

        drawDiagramTop();
    }

    void drawSamples() {
        final int sampleTopMargin = 25;
        final int sampleHeight = 10;
        final int sampleWidth = 150;
        final int sampleTextYOffset = 16;
        setGradient(mapLeftMargin, sampleTopMargin, sampleWidth, sampleHeight, color(MAX_LIGHT), color(0), 2);
        setGradient(mapLeftMargin, sampleTopMargin + mapHeight + mapTopMargin * 3, sampleWidth, sampleHeight, color(MAX_LIGHT), color(0), 2);
        textSize(12);
        fill(0);
        textAlign(LEFT);
        text(String.format("%.2f", minRatio * 100) + "%", mapLeftMargin, sampleTopMargin + sampleHeight + sampleTextYOffset);
        text(minAbs, mapLeftMargin, mapHeight + mapTopMargin * 3 + sampleTopMargin + sampleHeight + sampleTextYOffset);
        textAlign(RIGHT);
        text(String.format("%.2f", maxRatio * 100) + "%", mapLeftMargin + sampleWidth, sampleTopMargin + sampleHeight + sampleTextYOffset);
        text(maxAbs, mapLeftMargin + sampleWidth, mapHeight + mapTopMargin * 3 + sampleTopMargin + sampleHeight + sampleTextYOffset);
    }

    TableRow getSelectedRow() {
        // nagyon nem szép de csak így működött
        TableRow row = null;
        int rowIndex = (int)(selectedMax * (table.getRowCount() - 1));
        if (rowIndex < 2) {
            rowIndex = 2;
        }
        int i = 1;
        for (TableRow crow : table.rows()) {
            if (i == rowIndex) {
                row = crow;
                break;
            }
            i++;
        }
        return row;
    }

    void drawDiagramTop() {
        fill(0);
        final float axisThickness = 2;
        final float xAxisBeginX = width / 2 + chartLeftMargin;
        final float yAxisHeight = height / 2 - 2 * chartTopMargin;
        final float xAXisWidth = width / 2 - 2 * chartLeftMargin;
        rectMode(CORNER);
        rect(xAxisBeginX, chartTopMargin, axisThickness, yAxisHeight);
        rect(xAxisBeginX, chartTopMargin + yAxisHeight, xAXisWidth, axisThickness);

    }

    @Override
    public void mouseMoved() {
        setupActiveBar();
        redraw();
    }

    @Override
    public void mouseDragged() {
        if (maxActive) {
            mouseLockedMax = true;
            final float tempMax = map(mouseX, mapLeftMargin, mapLeftMargin + mapWidth, 0, 1);
            if (tempMax >= selectedMin && tempMax <= 1) {
                selectedMax = tempMax;
            }
        }
        if (minActive) {
            mouseLockedMin = true;
            final float tempMin = map(mouseX, mapLeftMargin, mapLeftMargin + mapWidth, 0, 1);
            if (tempMin <= selectedMax && tempMin >= 0) {
                selectedMin = tempMin;
            }
        }
        redraw();
    }

    @Override
    public void mouseReleased() {
        mouseLockedMax = false;
        mouseLockedMin = false;
        setupActiveBar();
        redraw();
    }

    final float STEP = 0.05f;
    @Override
    public void keyPressed() {
        switch(key) {
            case 'a':
                incrementMin(-1 * STEP);
                break;
            case 's':
                incrementMin(STEP);
                break;
            case 'd':
                incrementMax(-1 * STEP);
                break;
            case 'f':
                incrementMax(STEP);
                break;
        }
        setupActiveBar();
        redraw();
    }

    void incrementMin(final float value) {
        if (value > 0) {
            selectedMin = (selectedMin + value < selectedMax) ? (selectedMin + value) : selectedMax;
        }
        else {
            selectedMin = (selectedMin + value > 0) ? (selectedMin + value) : 0;
        }
    }
    void incrementMax(final float value) {
        if (value > 0) {
            selectedMax = (selectedMax + value < 1) ? (selectedMax + value) : 1;
        }
        else {
            selectedMax = (selectedMax + value > selectedMin) ? (selectedMax + value) : selectedMin;
        }
    }

    void setupRollbar() {
        fill(150, 20, 20);
        rectMode(CENTER);
        rect(mapLeftMargin + mapWidth / 2, rollbarCenterY, mapWidth, rollbarHeight / 3, 7);

        fill(0);
        rect(mapLeftMargin + selectedMin * mapWidth, rollbarCenterY, miniboxW + 2, miniboxH + 2, 2);
        fill(20, 100, minActive ? 200 : 10);
        rect(mapLeftMargin + selectedMin * mapWidth, rollbarCenterY, miniboxW, miniboxH, 2);

        fill(0);
        rect(mapLeftMargin + selectedMax * mapWidth, rollbarCenterY, miniboxW + 2, miniboxH + 2, 2);
        fill(20, 100, maxActive ? 200 : 10);
        rect(mapLeftMargin + selectedMax * mapWidth, rollbarCenterY, miniboxW, miniboxH, 2);
    }

    void shapeTop(final PShape countie) {
        shape(countie, mapLeftMargin, mapTopMargin, mapWidth, mapHeight);
    }

    void shapeBottom(final PShape countie) {
        shape(countie, mapLeftMargin, mapHeight + 3 * mapTopMargin, mapWidth, mapHeight);
    }


    // https://processing.org/examples/lineargradient.html
    int Y_AXIS = 1;
    int X_AXIS = 2;
    void setGradient(int x, int y, float w, float h, int c1, int c2, int axis ) {

        noFill();

        if (axis == Y_AXIS) {  // Top to bottom gradient
            for (int i = y; i <= y+h; i++) {
                float inter = map(i, y, y+h, 0, 1);
                int c = lerpColor(c1, c2, inter);
                stroke(c);
                line(x, i, x+w, i);
            }
        }
        else if (axis == X_AXIS) {  // Left to right gradient
            for (int i = x; i <= x+w; i++) {
                float inter = map(i, x, x+w, 0, 1);
                int c = lerpColor(c1, c2, inter);
                stroke(c);
                line(i, y, i, y+h);
            }
        }
        noStroke();
    }



    public static void main(String[] args) {
        PApplet.main(Application.class.getName());
    }
}
