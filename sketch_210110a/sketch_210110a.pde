import processing.core.PApplet;
import processing.core.PShape;
import processing.data.Table;
import processing.data.TableRow;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

    private final String MAP_NAME = "world.svg";

    private Table data;
    private PShape world;
    private List<CountryInfo> infoList = new ArrayList<CountryInfo>();

    @Override
    public void settings() {
        smooth();
        fullScreen();
    }

    @Override
    public void setup() {
        //world = loadShape(MAP_NAME);
        data = loadTable("owid-co2-data.csv", "header");
        for (TableRow row: data.rows()) {
            final CountryInfo countryInfo = new CountryInfo(
                    row.getString(0),
                    row.getString(1),
                    row.getInt(2),
                    row.getDouble(3),
                    row.getDouble(4),
                    row.getDouble(5),
                    row.getDouble(6),
                    row.getDouble(7),
                    row.getDouble(8),
                    row.getDouble(9),
                    row.getDouble(10),
                    row.getDouble(11),
                    row.getDouble(12),
                    row.getDouble(13),
                    row.getDouble(14),
                    row.getDouble(15),
                    row.getDouble(16),
                    row.getDouble(17),
                    row.getDouble(18),
                    row.getDouble(19),
                    row.getDouble(20),
                    row.getDouble(21),
                    row.getDouble(22),
                    row.getDouble(23),
                    row.getDouble(24),
                    row.getDouble(25),
                    row.getDouble(26),
                    row.getDouble(27),
                    row.getDouble(28),
                    row.getDouble(29),
                    row.getDouble(30),
                    row.getDouble(31),
                    row.getDouble(32),
                    row.getDouble(33),
                    row.getDouble(34),
                    row.getDouble(35),
                    row.getDouble(36),
                    row.getDouble(37)
            );
            infoList.add(countryInfo);
        }
        noLoop();
    }

    @Override
    public void draw() {
        background(255f);

        drawDiagram(1, 3, "Szöveg");
        drawDiagram(2, 3, "Szöveg");
        drawDiagram(3, 3, "Szöveg");
        drawDiagram(3, 1, "Szöveg");
    }

    private void alma(final String fieldName) throws NoSuchFieldException {
        final Field selectedField = CountryInfo.class.getField("isoCode");
    }

    final int MARGIN_TOP = 10;
    final int MARGIN_BOTTOM = 50;
    final int MARGIN_LEFT = 30;
    final int MARGIN_RIGHT = 5;
    final int AXIS_THICKNESS = 2;
    final float DIAGRAM_RELATIVE_SIZE = (float)1/3;

    private void drawDiagram(final int noX, final int noY, final String title) {

        final float relativeX = (noX - 1) * DIAGRAM_RELATIVE_SIZE;
        final float relativeY = (noY - 1) * DIAGRAM_RELATIVE_SIZE;

        final int xAxisWidth = (int)(width * DIAGRAM_RELATIVE_SIZE) - (MARGIN_LEFT + MARGIN_RIGHT);
        final int xAxisMin = (int)(width * relativeX) + MARGIN_LEFT;
        final int xAxisMax = xAxisMin + xAxisWidth;
        final int xAxisCenter = (xAxisMin + xAxisMax) / 2;
        final int yAxisHeight = (int)(height * DIAGRAM_RELATIVE_SIZE) - (MARGIN_TOP + MARGIN_BOTTOM);
        final int yAxisTop = (int)(height * relativeY) + MARGIN_TOP;
        final int yAxisBottom = yAxisTop + yAxisHeight;

        fill(0f);
        rectMode(CORNER);
        rect(xAxisMin, yAxisTop, AXIS_THICKNESS, yAxisHeight);
        rect(xAxisMin, yAxisBottom, xAxisWidth, AXIS_THICKNESS);

        textAlign(CENTER);
        textSize(12);
        text(title, xAxisCenter, yAxisBottom + MARGIN_BOTTOM - 5);
    }

//    private void drawDiagrams(final float relativeX) {
//        fill(0f);
//        rectMode(CORNER);
//
//        rect(xAxisMin, topDiagramYAxisMax, axisThickness, yAxisHeight);
//        rect(xAxisMin, topDiagramYAxisMin, xAxisWidth, axisThickness);
//
//        rect(xAxisMin, bottomDiagramYAxisMax, axisThickness, yAxisHeight);
//        rect(xAxisMin, bottomDiagramYAxisMin, xAxisWidth, axisThickness);
//
//        signAxes();
//
//        strokeWeight(2);
//        final int selectedRowNum = rowIndexMax - rowIndexMin + 1;
//        if (selectedRowNum > 1) {
//            for (String countieName : selectedCounties) {
//                int[][] positions = new int[selectedRowNum][3];  // x, y1, y2
//                for (int i = rowIndexMin; i <= rowIndexMax; i++) {
//                    final TableRow currentRow = getSelectedRowByIndex(i);
//                    positions[i - rowIndexMin][0] = (int) map(i, rowIndexMin, rowIndexMax, xAxisMin, xAxisMax);
//                    positions[i - rowIndexMin][1] = (int) map(getAffectedRatio(currentRow, countieName), 0, maxSelectedRatio, topDiagramYAxisMin, topDiagramYAxisMax);
//                    positions[i - rowIndexMin][2] = (int) map(getAbsoluteAffected(currentRow, countieName), 0, maxSelectedAbsolute, bottomDiagramYAxisMin, bottomDiagramYAxisMax);
//                }
//                stroke(colorOf(countieName));
//                for (int i = 1; i < selectedRowNum; i++) {
//                    line(positions[i - 1][0], positions[i - 1][1], positions[i][0], positions[i][1]);
//                    line(positions[i - 1][0], positions[i - 1][2], positions[i][0], positions[i][2]);
//                }
//            }
//        }
//        noStroke();
//    }

    private static class DiagramData {
        public String title;
        public final List<DiagramDataElement> elements = new ArrayList<DiagramDataElement>();
    }

    private static class DiagramDataElement {
        public int x;
        public int y;
    }

    private static class CountryInfo {

        public String isoCode;
        public String country;
        public int year;
        public double co2;
        public double co2_growth_prct;
        public double co2_growth_abs;
        public double consumption_co2;
        public double trade_co2;
        public double trade_co2_share;
        public double co2_per_capita;
        public double consumption_co2_per_capita;
        public double share_global_co2;
        public double cumulative_co2;
        public double share_global_cumulative_co2;
        public double co2_per_gdp;
        public double consumption_co2_per_gdp;
        public double co2_per_unit_energy;
        public double cement_co2;
        public double coal_co2;
        public double flaring_co2;
        public double gas_co2;
        public double oil_co2;
        public double cement_co2_per_capita;
        public double coal_co2_per_capita;
        public double flaring_co2_per_capita;
        public double gas_co2_per_capita;
        public double oil_co2_per_capita;
        public double total_ghg;
        public double ghg_per_capita;
        public double methane;
        public double methane_per_capita;
        public double nitrous_oxide;
        public double nitrous_oxide_per_capita;
        public double primary_energy_consumption;
        public double energy_per_capita;
        public double energy_per_gdp;
        public double population;
        public double gdp;

        public CountryInfo(String isoCode, String country, int year, double co2, double co2_growth_prct, double co2_growth_abs, double consumption_co2, double trade_co2, double trade_co2_share, double co2_per_capita, double consumption_co2_per_capita, double share_global_co2, double cumulative_co2, double share_global_cumulative_co2, double co2_per_gdp, double consumption_co2_per_gdp, double co2_per_unit_energy, double cement_co2, double coal_co2, double flaring_co2, double gas_co2, double oil_co2, double cement_co2_per_capita, double coal_co2_per_capita, double flaring_co2_per_capita, double gas_co2_per_capita, double oil_co2_per_capita, double total_ghg, double ghg_per_capita, double methane, double methane_per_capita, double nitrous_oxide, double nitrous_oxide_per_capita, double primary_energy_consumption, double energy_per_capita, double energy_per_gdp, double population, double gdp) {
            this.isoCode = isoCode;
            this.country = country;
            this.year = year;
            this.co2 = co2;
            this.co2_growth_prct = co2_growth_prct;
            this.co2_growth_abs = co2_growth_abs;
            this.consumption_co2 = consumption_co2;
            this.trade_co2 = trade_co2;
            this.trade_co2_share = trade_co2_share;
            this.co2_per_capita = co2_per_capita;
            this.consumption_co2_per_capita = consumption_co2_per_capita;
            this.share_global_co2 = share_global_co2;
            this.cumulative_co2 = cumulative_co2;
            this.share_global_cumulative_co2 = share_global_cumulative_co2;
            this.co2_per_gdp = co2_per_gdp;
            this.consumption_co2_per_gdp = consumption_co2_per_gdp;
            this.co2_per_unit_energy = co2_per_unit_energy;
            this.cement_co2 = cement_co2;
            this.coal_co2 = coal_co2;
            this.flaring_co2 = flaring_co2;
            this.gas_co2 = gas_co2;
            this.oil_co2 = oil_co2;
            this.cement_co2_per_capita = cement_co2_per_capita;
            this.coal_co2_per_capita = coal_co2_per_capita;
            this.flaring_co2_per_capita = flaring_co2_per_capita;
            this.gas_co2_per_capita = gas_co2_per_capita;
            this.oil_co2_per_capita = oil_co2_per_capita;
            this.total_ghg = total_ghg;
            this.ghg_per_capita = ghg_per_capita;
            this.methane = methane;
            this.methane_per_capita = methane_per_capita;
            this.nitrous_oxide = nitrous_oxide;
            this.nitrous_oxide_per_capita = nitrous_oxide_per_capita;
            this.primary_energy_consumption = primary_energy_consumption;
            this.energy_per_capita = energy_per_capita;
            this.energy_per_gdp = energy_per_gdp;
            this.population = population;
            this.gdp = gdp;
        }
    }
