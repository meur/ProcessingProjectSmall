PShape hungary;
Table table;

void setup() {
  size(1100, 700);  
  hungary = loadShape("HU_counties_blank.svg");
  table = loadTable("data.csv", "header");
}

void draw() {
  background(255);
  
  // Draw the full map
  shape(hungary, 0, 0);
  
  
  
  //for (TableRow row : table.rows()) {
    //println();
    TableRow lakossag = table.findRow("lakossag", "date");
    TableRow row = table.findRow("2020-12-01", "date");
    for (int m = 1; m < row.getColumnCount() - 1; m++) {
      final String countieName = row.getColumnTitle(m);
      println(countieName);
      
      final int lak = lakossag.getInt(countieName);
      final int cov = row.getInt(countieName);
      double arany = (double)cov / lak;
      arany *= 10;
       //<>//
      final PShape countie = hungary.getChild(countieName);
      countie.disableStyle();
      println(arany * 255);
      fill((int)(arany * 255), (int)(arany * 255), (int)(arany * 255));
      noStroke();
      shape(countie, 0, 0);
    }
  //}
}
