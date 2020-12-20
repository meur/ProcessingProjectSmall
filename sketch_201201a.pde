PShape hungary; //<>//
Table table;
TableRow lakossag;

int mapWidth;
int mapHeight;
int mapLeftMargin;
int mapTopMargin;

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

void setup() {
  size(1356, 710);
  hungary = loadShape("HU_counties_blank.svg");
  table = loadTable("data.csv", "header");

  lakossag = table.findRow("lakossag", "date"); //<>//

  setupShapeSize();
  setupActiveBar();
  
  rollbarCenterY = height - rollbarHeight / 2;
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

void draw() {
  background(255);
  
  mouseOverMax = (Math.abs(rollbarCenterY - mouseY) < miniboxH / 2 + 2) && (Math.abs(map(selectedMax, 0, 1, mapLeftMargin, mapLeftMargin + mapWidth) - mouseX) < miniboxW / 2 + 2);
  mouseOverMin = !mouseOverMax &&
                 (Math.abs(rollbarCenterY - mouseY) < miniboxH / 2 + 2) && (Math.abs(map(selectedMin, 0, 1, mapLeftMargin, mapLeftMargin + mapWidth) - mouseX) < miniboxW / 2 + 2);
  
  setupRollbar();
  
  TableRow row = getSelectedRow();
  
  float maxRatio = 0;
  float minRatio = 1;
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
  }
  
  for (int m = 1; m < row.getColumnCount() - 1; m++) {
    final String countieName = row.getColumnTitle(m);

    final int lak = lakossag.getInt(countieName);
    final int cov = row.getInt(countieName);
    float arany = ((float)cov / lak);
    final int colour0 = (int)map(arany, minRatio, maxRatio, 240, 0);
    
    final PShape countie = hungary.getChild(countieName);
    countie.disableStyle();
    
    fill(colour0, colour0, colour0);
    noStroke();
    shapeTop(countie);
    shapeBottom(countie);
  }
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

void mouseMoved() {
  setupActiveBar();
}

void mouseDragged() {
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
}

void mouseReleased() {
  mouseLockedMax = false;
  mouseLockedMin = false;
  setupActiveBar();
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
