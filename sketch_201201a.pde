PShape hungary; //<>//
Table table;
TableRow lakossag;
int mapWidth;
int mapHeight;
int mapLeftMargin;
int mapTopMargin;
final int rollbarHeight = 30;

void setup() {
  size(1356, 710);
  background(255);
  hungary = loadShape("HU_counties_blank.svg");
  table = loadTable("data.csv", "header");

  lakossag = table.findRow("lakossag", "date");

  setupShapeSize();
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

void shapeTop(final PShape countie) {
  shape(countie, mapLeftMargin, mapTopMargin, mapWidth, mapHeight);
}

void shapeBottom(final PShape countie) {
  shape(countie, mapLeftMargin, mapHeight + 3 * mapTopMargin, mapWidth, mapHeight);
}

void draw() {
  
  TableRow row = table.findRow("2020-12-01", "date");
  for (int m = 1; m < row.getColumnCount() - 1; m++) {
    final String countieName = row.getColumnTitle(m);
    

    final int lak = lakossag.getInt(countieName);
    final int cov = row.getInt(countieName);
    double arany = (double)cov / lak;
    arany *= 10;

    final PShape countie = hungary.getChild(countieName);
    countie.disableStyle();
    
    fill((int)(arany * 255), (int)(arany * 255), (int)(arany * 255));
    noStroke();
    shapeTop(countie);
    shapeBottom(countie);
  }
}
