import geomerative.RG;
import geomerative.RPoint;
import geomerative.RShape;
import processing.core.PApplet;
import processing.core.PShape;
import processing.data.Table;
import processing.data.TableRow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


    PShape hungary;
    Table table;
    TableRow population;

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

    final int chartTopMargin = 40;
    final int chartLeftMargin = 50;
    final float axisThickness = 2;
    final Set<String> selectedCounties = new HashSet<String>();
    String hoveredCountie = null;

    boolean mouseOverTopChart;
    boolean mouseOverBottomChart;

    int rowIndexMin;
    int rowIndexMax;

    RShape grp;

    private static final String SVG = "HU_counties_blank.svg";
    
    private float maxSelectedRatio;
    private float maxSelectedAbsolute;
    private float xAxisMin;
    private float xAxisMax;
    private float xAxisWidth;
    private float yAxisHeight;
    private float topDiagramYAxisMax;
    private float topDiagramYAxisMin;
    private float bottomDiagramYAxisMax;
    private float bottomDiagramYAxisMin;
    private int bottomMapBottom;
    private int bottomMapTop;

    @Override
    public void settings() {
        smooth();
        fullScreen();
    }

    private void initGeomerative() {
        RG.init(this);
        RG.ignoreStyles(true);
        RG.setPolygonizer(RG.ADAPTATIVE);
        grp = RG.loadShape(SVG);
    }

    @Override
    public void setup() {
        initGeomerative();
        noStroke();
        frameRate(30);
        hungary = loadShape(SVG);
        table = loadTable("data.csv", "header");

        population = table.findRow("lakossag", "date");

        selectedCounties.add("hb");
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
        bottomMapTop = mapTopMargin * 3 + mapHeight;
        bottomMapBottom = mapTopMargin * 3 + mapHeight * 2;
    }

    @Override
    public void draw() {
        background(color(252, 255, 252));

        setVariables();
        setHoveredCountie();
        processMousePosition();
        setupRollbar();
        drawMaps();
        drawMapTooltip();
        drawSamples();
        drawDiagrams();
        drawDiagramColorInfo();
        drawInfoAxis();
    }

    private void setVariables() {
        rowIndexMin = getSelectedRowIndex(selectedMin);
        rowIndexMax = getSelectedRowIndex(selectedMax);
        setMaxSelectedRatio();
        setMaxSelectedAbsolute();
        xAxisMin = width / 2 + chartLeftMargin;
        xAxisMax = xAxisMin + xAxisWidth;
        xAxisWidth = width / 2 - 2 * chartLeftMargin;
        yAxisHeight = height / 2 - 2 * chartTopMargin;
        topDiagramYAxisMax = chartTopMargin;
        topDiagramYAxisMin = chartTopMargin + yAxisHeight;
        bottomDiagramYAxisMax = chartTopMargin * 3 + yAxisHeight;
        bottomDiagramYAxisMin = chartTopMargin * 3 + yAxisHeight * 2;
    }

    private void drawDiagramColorInfo() {
        if (selectedCounties.size() > 0) {
            final int count = selectedCounties.size();
            final int minX = width / 2;
            final int possibleWidth = minX;
            final int step = possibleWidth / (count);
            int index = 0;
            final int y = height / 2 - mapTopMargin / 2;
            for (String countieName : selectedCounties) {
                final int x = minX + index * step + (int) ((float) width / (4 * count));
                drawCircle(x, y, countieName);
                fill(0f);

                pushMatrix();
                translate(x - 11, y + 11);
                rotate(radians(45));
                text(Countie.valueOf(countieName).getShortName(), 0, 0);
                popMatrix();

                line(0, 0, 150, 0);
                index++;
            }
        }
    }

    private void drawMapTooltip() {
        if (hoveredCountie != null) {
            final TableRow row = getSelectedRow(selectedMax);
            final int absolute = getAbsoluteAffected(row, hoveredCountie);
            final float ratio = getAffectedRatio(row, hoveredCountie) * 100;
            final String fullName = Countie.valueOf(hoveredCountie).fullName;
            final List<String> lines = new ArrayList<String>();
            lines.add(String.format("%.2f", ratio) + "%");
            lines.add(String.format("%,d", absolute));
            drawToolTip(50, 150, fullName, lines, false);
        }
    }

    private void drawToolTip(final int minWidth, final int maxWidth, final String title, final List<String> lines,
                             final boolean left) {
        rectMode(CORNER);
        textAlign(LEFT);
        stroke(0f);
        strokeWeight(1);
        fill(color(254, 240, 220));
        int maxlen = title.length();
        for (String line: lines) {
            maxlen = max(maxlen, line.length());
        }
        final int boxWidth = max(minWidth, min(maxWidth, maxlen * 9));
        final int boxHeight = 16 * (lines.size() + 1) + 5;
        int x = left
                ? mouseX - 5 - boxWidth
                : min(mouseX + 12, width - boxWidth);
        final int y = left && mouseY > height / 2
                ? mouseY - boxHeight
                : min(mouseY + 16, height - boxHeight);
        rect(x, y, boxWidth, boxHeight, 7);
        fill(0f);
        text(title, x + 5, y + 16);
        for (int i = 0; i < lines.size(); i++) {
            text(lines.get(i), x + 5, y + 16 * (i + 2));
        }
        noStroke();
    }

    private void setHoveredCountie() {
        final int mappedX = (int)map(mouseX, mapLeftMargin, mapLeftMargin + mapWidth, 0, hungary.width);
        final int mappedY1 = (int)map(mouseY, mapTopMargin, mapTopMargin + mapHeight, 0, hungary.height);
        final int mappedY2 = (int)map(mouseY, bottomMapTop, bottomMapBottom, 0, hungary.height);

        hoveredCountie = null;
        RPoint p1 = new RPoint(mappedX, mappedY1);
        RPoint p2 = new RPoint(mappedX, mappedY2);
        for(int i = 0; i < grp.children[0].countChildren(); i++) {
            if(grp.children[0].children[i].contains(p1) || grp.children[0].children[i].contains(p2)){
                hoveredCountie = (grp.children[0].children[i].name);
            }
        }
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
            final int absolute = getAbsoluteAffected(row, countieName);
            final float ratio = getAffectedRatio(row, countieName);
            maxRatio = max(ratio, maxRatio);
            minRatio = min(ratio, minRatio);
            maxAbs = max(absolute, maxAbs);
            minAbs = min(absolute, minAbs);
        }

        for (int m = 1; m < row.getColumnCount() - 1; m++) {
            final String countieName = row.getColumnTitle(m);

            final int currentPopulation = population.getInt(countieName);
            final int absolute = row.getInt(countieName);
            float ratio = ((float)absolute / currentPopulation);
            final int colour0 = getColor(map(ratio, minRatio, maxRatio, 0, 1));
            final int colour1 = getColor(map(absolute, minAbs, maxAbs, 0, 1));

            final PShape countie = hungary.getChild(countieName);
            countie.disableStyle();

            stroke(255f);
            strokeWeight(2);
            fill(colour0);
            shapeTop(countie);
            fill(colour1);
            shapeBottom(countie);

            // circles for selected counties
            if (selectedCounties.contains(countieName)) {
                final RPoint centroid = grp.getChild(countieName).getCentroid();
                final int mappedX = (int)map(centroid.x, 0, hungary.width, mapLeftMargin, mapLeftMargin + mapWidth);
                final int mappedY1 = (int)map(centroid.y, 0, hungary.height, mapTopMargin, mapTopMargin + mapHeight);
                final int mappedY2 = (int)map(centroid.y, 0, hungary.height, bottomMapTop, bottomMapBottom);
                drawCircle(mappedX, mappedY1, countieName);
                drawCircle(mappedX, mappedY2, countieName);
            }
        }
        noStroke();
    }

    private void drawCircle(final int x, final int y, final String countieName) {
        ellipseMode(RADIUS);
        stroke(255f);
        strokeWeight(2);
        fill(colorOf(countieName));
        ellipse(x, y, 6, 6);
        noStroke();
    }

    private void drawSamples() {
        noStroke();
        final int sampleTopMargin = 25;
        final int sampleHeight = 10;
        final int sampleWidth = 150;
        final int sampleTextYOffset = 16;
        setGradient(mapLeftMargin, sampleTopMargin, sampleWidth, sampleHeight, color(MAX_LIGHT), color(0), 2);
        setGradient(mapLeftMargin, sampleTopMargin + bottomMapTop, sampleWidth, sampleHeight, color(MAX_LIGHT), color(0), 2);
        textSize(12);
        fill(0);
        textAlign(LEFT);
        text(String.format("%.2f", minRatio * 100) + "%", mapLeftMargin, sampleTopMargin + sampleHeight + sampleTextYOffset);
        text(String.format("%,d", minAbs), mapLeftMargin, bottomMapTop + sampleTopMargin + sampleHeight + sampleTextYOffset);
        textAlign(RIGHT);
        text(String.format("%.2f", maxRatio * 100) + "%", mapLeftMargin + sampleWidth, sampleTopMargin + sampleHeight + sampleTextYOffset);
        text(String.format("%,d",maxAbs), mapLeftMargin + sampleWidth, bottomMapTop + sampleTopMargin + sampleHeight + sampleTextYOffset);
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

        rect(xAxisMin, topDiagramYAxisMax, axisThickness, yAxisHeight);
        rect(xAxisMin, topDiagramYAxisMin, xAxisWidth, axisThickness);

        rect(xAxisMin, bottomDiagramYAxisMax, axisThickness, yAxisHeight);
        rect(xAxisMin, bottomDiagramYAxisMin, xAxisWidth, axisThickness);

        signAxes();

        strokeWeight(2);
        final int selectedRowNum = rowIndexMax - rowIndexMin + 1;
        if (selectedRowNum > 1) {
            for (String countieName : selectedCounties) {
                int[][] positions = new int[selectedRowNum][3];  // x, y1, y2
                for (int i = rowIndexMin; i <= rowIndexMax; i++) {
                    final TableRow currentRow = getSelectedRowByIndex(i);
                    positions[i - rowIndexMin][0] = (int) map(i, rowIndexMin, rowIndexMax, xAxisMin, xAxisMax);
                    positions[i - rowIndexMin][1] = (int) map(getAffectedRatio(currentRow, countieName), 0, maxSelectedRatio, topDiagramYAxisMin, topDiagramYAxisMax);
                    positions[i - rowIndexMin][2] = (int) map(getAbsoluteAffected(currentRow, countieName), 0, maxSelectedAbsolute, bottomDiagramYAxisMin, bottomDiagramYAxisMax);
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

    private void drawInfoAxis() {
        mouseOverTopChart = (mouseX > xAxisMin && mouseX < xAxisMax)
                && (mouseY > topDiagramYAxisMax && mouseY < topDiagramYAxisMin);
        mouseOverBottomChart = (mouseX > xAxisMin && mouseX < xAxisMax)
                && (mouseY > bottomDiagramYAxisMax && mouseY < bottomDiagramYAxisMin);

        if (mouseOverTopChart || mouseOverBottomChart) {
            int axisBottom = 0;
            int axisTop = 0;
            final List<String> lines = new ArrayList<String>();
            final int rowIndex = (int)map(mouseX, xAxisMin, xAxisMax - axisThickness, rowIndexMin, rowIndexMax);
            final TableRow cRow = getSelectedRowByIndex(rowIndex);
            if (mouseOverTopChart) {
                axisBottom = (int)topDiagramYAxisMin;
                axisTop = (int)topDiagramYAxisMax;
                for (String countie: selectedCounties) {
                    lines.add(String.format("%s: %.2f", Countie.valueOf(countie).fullName, getAffectedRatio(cRow, countie) * 100));
                }
            }
            else if (mouseOverBottomChart) {
                axisBottom = (int)bottomDiagramYAxisMin;
                axisTop = (int)bottomDiagramYAxisMax;
                for (String countie: selectedCounties) {
                    lines.add(String.format("%s: %,d", Countie.valueOf(countie).fullName, getAbsoluteAffected(cRow, countie)));
                }
            }
            strokeWeight(1);
            stroke(0f);
            line(mouseX, axisBottom, mouseX, axisTop);
            drawToolTip(50, 200, cRow.getString(0), lines, true);
            noStroke();
        }
    }

    private void signAxes() {

        int num = (int)yAxisHeight / 50;
        int step = (int)yAxisHeight / num;
        final int signRightMargin = 5;
        textAlign(RIGHT);
        for (int i = 0; i <= num; i++) {
            text(String.format("%.2f", maxSelectedRatio * (num - i) / num * 100) + "%", xAxisMin - signRightMargin, topDiagramYAxisMax + i * step);
            text(String.format("%,.0f", maxSelectedAbsolute * (num - i) / num), xAxisMin - signRightMargin, bottomDiagramYAxisMax + i * step);
        }

        rectMode(CORNER);

        num = (int)xAxisWidth / 100;
        step = (int)xAxisWidth / num;
        final int signTopMargin = 20;
        textAlign(CENTER);
        for (int i = 0; i <= num; i++) {
            fill(0f);
            final int rowIndex = (int)map(i, 0, num, rowIndexMin, rowIndexMax);
            final String datum = getSelectedRowByIndex(rowIndex).getString(0);
            text(datum, xAxisMin + i * step, topDiagramYAxisMin + signTopMargin);
            text(datum, xAxisMin + i * step, bottomDiagramYAxisMin + signTopMargin);

            if (i > 0) {
                fill(200f);
                rect(xAxisMin + i * step + axisThickness, bottomDiagramYAxisMin, 1, -1 * yAxisHeight);
                rect(xAxisMin + i * step + axisThickness, topDiagramYAxisMin, 1, -1 * yAxisHeight);
            }
        }
    }

    private void setMaxSelectedRatio() {
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
        this.maxSelectedRatio = max;
    }

    private void setMaxSelectedAbsolute() {
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
        maxSelectedAbsolute = max;
    }

    private float getAffectedRatio(final TableRow row, final String countieName) {
        final int lak = population.getInt(countieName);
        final int cov = getAbsoluteAffected(row, countieName);
        return (float)cov / lak;
    }

    private int getAbsoluteAffected(final TableRow row, final String countieName) {
        return row.getInt(countieName);
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

    @Override
    public void mouseClicked() {
        if (hoveredCountie != null) {
            if (selectedCounties.contains(hoveredCountie)) {
                selectedCounties.remove(hoveredCountie);
            }
            else {
                selectedCounties.add(hoveredCountie);
            }
        }
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
            case 'c':
                selectedCounties.clear();
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
        strokeWeight(1);

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

    public enum Countie {
        bk("Bács-Kiskun", "BK",  81,87,74),
        baranya("Baranya", 68, 124, 105),
        bekes("Békés", 116, 196, 147),
        baz("Borsod-Abaúj-Zemplén", "BAZ", 142, 140, 109),
        budapest("Budapest", 233, 215, 142),
        csongrad("Csongrád-Csanád", "Csongrád",  228, 191, 128),
        fejer("Fejér", 233, 215, 142),
        gyms("Györ-Moson-Sopron", "GyMS", 226, 151, 93),
        hb("Hajdú-Bihar", "HB", 241, 150, 112),
        heves("Heves", 225, 101, 82),
        jnsz("Jász-Nagykun-Szolnok", "JNSz", 201, 74, 83),
        ke("Komárom-Esztergom", "KE", 190, 81, 104),
        nograd("Nógrád", 163, 73, 116),
        pest("Pest", 153, 55, 103),
        somogy("Somogy", 101, 56, 125),
        szszb("Szabolcs-Szatmár-Bereg", "SzSzB", 78, 36, 114),
        tolna("Tolna", 145, 99, 182),
        vas("Vas", 226, 121, 163),
        veszprem("Veszprém", 86, 152, 196),
        zala("Zala", 124, 159, 176);

        public String fullName;
        private String shortName;
        public int r;
        public int g;
        public int b;

        public String getShortName() {
            return shortName != null ? shortName : fullName;
        }

        private Countie(final String fullName, final int r, final int g, final int b) {
            this.fullName = fullName;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        private Countie(final String fullName, final String shortName, final int r, final int g, final int b) {
            this.fullName = fullName;
            this.shortName = shortName;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    private int colorOf(final String countieName) {
        final Countie countie = Countie.valueOf(countieName);
        return color(countie.r, countie.g, countie.b);
    }
