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
    final int chartLeftMargin = 50;
    final float axisThickness = 2;
    final Set<String> selectedCounties = new HashSet<String>();

    int rowIndexMin;
    int rowIndexMax;

    @Override
    public void settings() {
        size(1356, 710);
        smooth(8);
    }

    @Override
    public void setup() {
        noStroke();
        frameRate(30);
        hungary = loadShape("HU_counties_blank.svg");
        table = loadTable("data.csv", "header");

        lakossag = table.findRow("lakossag", "date");

        selectedCounties.add("vas");
        selectedCounties.add("somogy");
        selectedCounties.add("gyms");
        selectedCounties.add("budapest");

        setupShapeSize();
        setupActiveBar();

        rollbarCenterY = height - rollbarHeight / 2;
        noLoop();
    }

    private void setupActiveBar() {
        maxActive = (mouseOverMax || mouseLockedMax) && !mouseLockedMin;
        minActive = (mouseOverMin || mouseLockedMin) && !mouseLockedMax;
    }

    private void setupShapeSize() {
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

        rowIndexMin = getSelectedRowIndex(selectedMin);
        rowIndexMax = getSelectedRowIndex(selectedMax);

        processMousePosition();
        setupRollbar();
        drawMaps();
        drawSamples();
        drawDiagrams();
    }

    private void processMousePosition() {
        mouseOverMax = (Math.abs(rollbarCenterY - mouseY) < miniboxH / 2 + 2)
                && (Math.abs(map(selectedMax, 0, 1, mapLeftMargin, mapLeftMargin + mapWidth) - mouseX) < miniboxW / 2 + 2);
        mouseOverMin = !mouseOverMax
                && (Math.abs(rollbarCenterY - mouseY) < miniboxH / 2 + 2)
                && (Math.abs(map(selectedMin, 0, 1, mapLeftMargin, mapLeftMargin + mapWidth) - mouseX) < miniboxW / 2 + 2);
    }

    private void drawMaps() {
        final TableRow row = getSelectedRow(selectedMax);

        maxRatio = 0;
        minRatio = 1;
        maxAbs = 0;
        minAbs = 10000000;
        for (int m = 1; m < row.getColumnCount() - 1; m++) {
            final String countieName = row.getColumnTitle(m);

            final int cov = getAbsoluteAffected(row, countieName);
            final float arany = getAffectedRatio(row, countieName);
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

        for (int m = 1; m < row.getColumnCount() - 1; m++) {
            final String countieName = row.getColumnTitle(m);

            if (selectedCounties.contains(countieName)) {
                stroke(color(255, 0, 0));
                strokeWeight(2);
            }
            else {
                strokeWeight(1);
                stroke(255f);
            }

            final int lak = lakossag.getInt(countieName);
            final int cov = row.getInt(countieName);
            float arany = ((float)cov / lak);
            final int colour0 = getColor(map(arany, minRatio, maxRatio, 0, 1));
            final int colour1 = getColor(map(cov, minAbs, maxAbs, 0, 1));

            final PShape countie = hungary.getChild(countieName);
            countie.disableStyle();

            fill(colour0);
            shapeTop(countie);
            fill(colour1);
            shapeBottom(countie);
        }
        noStroke();
    }

    private void drawSamples() {
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
        text(String.format("%,d", minAbs), mapLeftMargin, mapHeight + mapTopMargin * 3 + sampleTopMargin + sampleHeight + sampleTextYOffset);
        textAlign(RIGHT);
        text(String.format("%.2f", maxRatio * 100) + "%", mapLeftMargin + sampleWidth, sampleTopMargin + sampleHeight + sampleTextYOffset);
        text(String.format("%,d",maxAbs), mapLeftMargin + sampleWidth, mapHeight + mapTopMargin * 3 + sampleTopMargin + sampleHeight + sampleTextYOffset);
    }

    private TableRow getSelectedRow(final float value) {
        // nagyon nem szép de csak így működött
        int rowIndex = getSelectedRowIndex(value);
        return getSelectedRowByIndex(rowIndex);
    }

    private int getSelectedRowIndex(final float value) {
        int rowIndex = (int)(value * (table.getRowCount() - 1));
        if (rowIndex < 2) {
            rowIndex = 2;
        }
        return rowIndex;
    }

    private TableRow getSelectedRowByIndex(final int index) {
        // nagyon nem szép de csak így működött
        TableRow row = null;
        int i = 1;
        for (TableRow crow : table.rows()) {
            if (i == index) {
                row = crow;
                break;
            }
            i++;
        }
        return row;
    }

    private void drawDiagrams() {
        fill(0);
        rectMode(CORNER);

        rect(getXAxisMin(), getTopDiagramYAxisMax(), axisThickness, getYAxisHeight());
        rect(getXAxisMin(), getTopDiagramYAxisMin(), getXAxisWidth(), axisThickness);

        rect(getXAxisMin(), getBottomDiagramYAxisMax(), axisThickness, getYAxisHeight());
        rect(getXAxisMin(), getBottomDiagramYAxisMin(), getXAxisWidth(), axisThickness);

        signAxes();

        strokeWeight(2);
        final int selectedRowNum = rowIndexMax - rowIndexMin + 1;
        if (selectedRowNum > 1) {
            final float maxSelectedRatio = getMaxSelectedRatio();
            final float maxSelectedAbs = getMaxSelectedAbsolute();
            for (String countieName : selectedCounties) {
                int[][] positions = new int[selectedRowNum][3];  // x, y1, y2
                for (int i = rowIndexMin; i <= rowIndexMax; i++) {
                    final TableRow currentRow = getSelectedRowByIndex(i);
                    positions[i - rowIndexMin][0] = (int) map(i, rowIndexMin, rowIndexMax, getXAxisMin(), getXAxisMax());
                    positions[i - rowIndexMin][1] = (int) map(getAffectedRatio(currentRow, countieName), 0, maxSelectedRatio, getTopDiagramYAxisMin(), getTopDiagramYAxisMax());
                    positions[i - rowIndexMin][2] = (int) map(getAbsoluteAffected(currentRow, countieName), 0, maxSelectedAbs, getBottomDiagramYAxisMin(), getBottomDiagramYAxisMax());
                }
                stroke(colorOf(countieName));
                for (int i = 1; i < selectedRowNum; i++) {
                    line(positions[i - 1][0], positions[i - 1][1], positions[i][0], positions[i][1]);
                    line(positions[i - 1][0], positions[i - 1][2], positions[i][0], positions[i][2]);
                }
            }
        }
        noStroke();
    }

    private void signAxes() {

        int num = (int)getYAxisHeight() / 50;
        int step = (int)getYAxisHeight() / num;
        final int signRightMargin = 5;
        textAlign(RIGHT);
        for (int i = 0; i <= num; i++) {
            text(String.format("%.2f", getMaxSelectedRatio() * (num - i) / num * 100) + "%", getXAxisMin() - signRightMargin, getTopDiagramYAxisMax() + i * step);
            text(String.format("%,.0f", getMaxSelectedAbsolute() * (num - i) / num), getXAxisMin() - signRightMargin, getBottomDiagramYAxisMax() + i * step);
        }

        rectMode(CORNER);

        num = (int)getXAxisWidth() / 100;
        step = (int)getXAxisWidth() / num;
        final int signTopMargin = 20;
        textAlign(CENTER);
        for (int i = 0; i <= num; i++) {
            fill(0f);
            final int rowIndex = (int)map(i, 0, num, rowIndexMin, rowIndexMax);
            final String datum = getSelectedRowByIndex(rowIndex).getString(0);
            text(datum, getXAxisMin() + i * step, getTopDiagramYAxisMin() + signTopMargin);
            text(datum, getXAxisMin() + i * step, getBottomDiagramYAxisMin() + signTopMargin);

            if (i > 0) {
                fill(200f);
                rect(getXAxisMin() + i * step, getBottomDiagramYAxisMin(), 1, -1 * getYAxisHeight());
                rect(getXAxisMin() + i * step, getTopDiagramYAxisMin(), 1, -1 * getYAxisHeight());
            }
        }
    }

    //TODO ezeket kivinni változóba ˇˇ
    private float getMaxSelectedRatio() {
        float max = 0;
        for (int i = rowIndexMin; i <= rowIndexMax; i++) {
            TableRow row = getSelectedRowByIndex(i);
            for (String name: selectedCounties) {
                final float currRatio = getAffectedRatio(row, name);
                if (currRatio > max) {
                    max = currRatio;
                }
            }
        }
        return max;
    }

    private float getMaxSelectedAbsolute() {
        float max = 0;
        for (int i = rowIndexMin; i <= rowIndexMax; i++) {
            TableRow row = getSelectedRowByIndex(i);
            for (String name: selectedCounties) {
                final float currAbs = getAbsoluteAffected(row, name);
                if (currAbs > max) {
                    max = currAbs;
                }
            }
        }
        return max;
    }

    private float getAffectedRatio(final TableRow row, final String countieName) {
        final int lak = lakossag.getInt(countieName);
        final int cov = getAbsoluteAffected(row, countieName);
        return (float)cov / lak;
    }

    private int getAbsoluteAffected(final TableRow row, final String countieName) {
        return row.getInt(countieName);
    }

    private float getXAxisMin() {
        return width / 2 + chartLeftMargin;
    }

    private float getXAxisMax() {
        return getXAxisMin() + getXAxisWidth();
    }

    private float getXAxisWidth() {
        return width / 2 - 2 * chartLeftMargin;
    }

    private float getYAxisHeight() {
        return height / 2 - 2 * chartTopMargin;
    }

    private float getTopDiagramYAxisMax() {
        return chartTopMargin;
    }

    private float getTopDiagramYAxisMin() {
        return chartTopMargin + getYAxisHeight();
    }

    private float getBottomDiagramYAxisMax() {
        return chartTopMargin * 3 + getYAxisHeight();
    }

    private float getBottomDiagramYAxisMin() {
        return chartTopMargin * 3 + getYAxisHeight() * 2;
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

    private void incrementMin(final float value) {
        if (value > 0) {
            selectedMin = min(selectedMin + value, selectedMax);
        }
        else {
            selectedMin = (selectedMin + value > 0) ? (selectedMin + value) : 0;
        }
    }
    private void incrementMax(final float value) {
        if (value > 0) {
            selectedMax = (selectedMax + value < 1) ? (selectedMax + value) : 1;
        }
        else {
            selectedMax = max(selectedMax + value, selectedMin);
        }
    }

    private void setupRollbar() {
        fill(150, 20, 20);
        rectMode(CENTER);
        rect(mapLeftMargin + mapWidth / 2, rollbarCenterY, mapWidth, rollbarHeight / 3, 7);

        strokeWeight(1);
        stroke(0f);

        fill(20, 100, minActive ? 200 : 10);
        rect(mapLeftMargin + selectedMin * mapWidth, rollbarCenterY, miniboxW, miniboxH, 2);

        fill(20, 100, maxActive ? 200 : 10);
        rect(mapLeftMargin + selectedMax * mapWidth, rollbarCenterY, miniboxW, miniboxH, 2);

        noStroke();
    }

    private void shapeTop(final PShape countie) {
        shape(countie, mapLeftMargin, mapTopMargin, mapWidth, mapHeight);
    }

    private void shapeBottom(final PShape countie) {
        shape(countie, mapLeftMargin, mapHeight + 3 * mapTopMargin, mapWidth, mapHeight);
    }

    // https://processing.org/examples/lineargradient.html
    private void setGradient(int x, int y, float w, float h, int c1, int c2, int axis ) {
        int Y_AXIS = 1;
        int X_AXIS = 2;
        noFill();

        if (axis == Y_AXIS) {  // Top to bottom gradient
            for (int i = y; i <= y+h; i++) {
                float inter = map(i, y, y+h, 0, 1);
                int c = getColor(inter);
                stroke(c);
                line(x, i, x+w, i);
            }
        }
        else if (axis == X_AXIS) {  // Left to right gradient
            for (int i = x; i <= x+w; i++) {
                float inter = map(i, x, x+w, 0, 1);
                int c = getColor(inter);
                stroke(c);
                line(i, y, i, y+h);
            }
        }
        noStroke();
    }

    private int getColor(final float ratio) {
        return lerpColor(color(254, 240, 220), color(180, 0, 0), ratio);
    }

    public static void main(String[] args) {
        PApplet.main(Application.class.getName());
    }

    public enum Countie {
        bk("Bács-Kiskun", 81,87,74),
        baranya("Baranya", 68, 124, 105),
        bekes("Békés", 116, 196, 147),
        baz("Borsod-Abaúj-Zemplén", 142, 140, 109),
        budapest("Budapest", 233, 215, 142),
        csongrad("Csongrád-Csanád", 228, 191, 128),
        fejer("Fejér", 233, 215, 142),
        gyms("Győr-Moson-Sopron", 226, 151, 93),
        hb("Hajdú-Bihar", 241, 150, 112),
        heves("Heves", 225, 101, 82),
        jnsz("Jász-Nagykun-Szolnok", 201, 74, 83),
        ke("Komárom-Esztergom", 190, 81, 104),
        nograd("Nógrád", 163, 73, 116),
        pest("Pest", 153, 55, 103),
        somogy("Somogy", 101, 56, 125),
        szszb("Szabolcs-Szatmár-Bereg", 78, 36, 114),
        tolna("Tolna", 145, 99, 182),
        vas("Vas", 226, 121, 163),
        veszprem("Veszprém", 86, 152, 196),
        zala("Zala", 124, 159, 176);

        public String fullName;
        public int r;
        public int g;
        public int b;

        Countie(final String fullName, final int r, final int g, final int b) {
            this.fullName = fullName;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    private int colorOf(final String countieName) {
        final Countie countie = Countie.valueOf(countieName);
        return color(countie.r, countie.g, countie.b);
    }
}