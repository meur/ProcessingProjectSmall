import processing.core.PApplet;
import processing.core.PShape;
import processing.data.Table;
import processing.data.TableRow;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class Application2 extends PApplet {

    private final String MAP_NAME = "world.svg";

    private static final int MIN_YEAR = 1990;
    private static final int MAX_YEAR = 2018;
    private int selectedYear = MAX_YEAR;

    private Table data;
    private PShape world;
    private final List<CountryInfo> infoList = new ArrayList<CountryInfo>();

    private int focusedPropertyIndex = 6;
    private Property focusedProperty;
    private int focusedCountryIndex = 3;
    private Country focusedCountry;
    private final EnumSet<Country> selectedCountries = EnumSet.of(
            Country.HUN, Country.DEU, Country.JPN, Country.AUS, Country.SDN, Country.NER,
            Country.USA, Country.RUS, Country.CHN, Country.IND, Country.BRA);

    @Override
    public void settings() {
        smooth();
        fullScreen();
    }

    @Override
    public void setup() {
        world = loadShape(MAP_NAME);
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

        setFocusedProperty();
        drawMap();
        drawBarChart();
        highlightSelectedCountries();
        setFocusedCountry();
        drawDiagramGrid();
    }

    private void drawDiagramGrid() {
        final String selectedCountry = focusedCountry.fullName;

        try {
            drawDiagram(1, 1, getDiagramData(selectedCountry, Property.ENERGY_CONSUMPTION));
            drawDiagram(1, 2, getDiagramData(selectedCountry, Property.ENERGY_PCPT));

            drawDiagram(2, 1, getDiagramData(selectedCountry, Property.POPULATION));
            drawDiagram(2, 2, getDiagramData(selectedCountry, Property.ENERGY_PGDP));

            drawDiagram(4, 2, getDiagramData(selectedCountry, Property.CO2_PCPT));
            drawDiagram(4, 1, getDiagramData(selectedCountry, Property.CO2));

            drawDiagram(3, 2, getDiagramData(selectedCountry, Property.NITROUS_OXIDE_PCPT));
            drawDiagram(3, 1, getDiagramData(selectedCountry, Property.NITROUS_OXIDE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DiagramData getDiagramData(final String countryName, final Property property)
            throws NoSuchFieldException, IllegalAccessException {
        final Field selectedField = CountryInfo.class.getField(property.code);
        final String title = countryName + " - " + property.fullName;
        final DiagramData data = new DiagramData(title);

        for (CountryInfo countryInfo: infoList) {
            if (countryInfo.year >= MIN_YEAR && countryInfo.country.equals(countryName)) {
                final DiagramDataElement newElement = new DiagramDataElement();
                newElement.x = countryInfo.year;
                newElement.y = selectedField.getDouble(countryInfo);

                data.elements.add(newElement);
            }
        }
        return data;
    }

    private int mapWidth;
    private int mapHeight;
    private int mapTopMargin;
    private final int mapLeftMargin = -100;
    private final int mapRightMargin = 100;

    private void drawMap() {
        world.disableStyle();
        noFill();
        stroke(1);

        mapTopMargin = height / 2;
        mapHeight = height - mapTopMargin;
        final float ratio = mapHeight / world.height;
        mapWidth = (int)(world.width * ratio);
        shapeMap(world);
    }

    public void shapeMap(final PShape shape) {
        super.shape(shape, mapLeftMargin, mapTopMargin, mapWidth, mapHeight);
    }

    private final int barChartRightMargin = 20;
    private int barChartLeftMargin;
    private int barChartTopMargin;
    private int barChartHeight;
    private int barChartWidth;
    private final int barChartBottomMargin = 50;
    private final int barHeight = 20;
    private final int minBarWidth = 2;
    List<BarChartData> barChartDataList;

    private void drawBarChart() {
        barChartLeftMargin = mapLeftMargin + mapWidth + mapRightMargin;
        barChartTopMargin = mapTopMargin + 40;
        barChartHeight = height - barChartTopMargin - barChartBottomMargin;
        barChartWidth = width - barChartLeftMargin - barChartRightMargin;
        final int barChartBottom = barChartTopMargin + barChartHeight;

        noStroke();
        fill(0f);
        rect(barChartLeftMargin, barChartTopMargin, AXIS_THICKNESS, barChartHeight);
        rect(barChartLeftMargin, barChartBottom, barChartWidth, AXIS_THICKNESS);

        barChartDataList = getBarChartDataList();
        Collections.sort(barChartDataList);
        final int barsCount = barChartDataList.size();
        final int spaceBetweenBars = (barChartHeight - barsCount * barHeight) / barsCount;
        final float maxValue = safeFloat(barChartDataList.get(barsCount - 1).value);
        for (int i = 0; i < barsCount; i++) {
            final BarChartData data = barChartDataList.get(i);
            int barWidth = (int)map(data.value.floatValue(), 0, maxValue, minBarWidth, barChartWidth) - AXIS_THICKNESS;
            if (barWidth < minBarWidth) {
                barWidth = 0;
            }
            final int barTop = (int)(barChartTopMargin + ((0.5f + i) * spaceBetweenBars) + (i * barHeight));
            final int color = lerpColor(color(198, 255, 221), color(247, 121, 125), (float)barWidth / barChartWidth);
            fill(color);
            data.color = color;
            rect(barChartLeftMargin + AXIS_THICKNESS, barTop, barWidth, barHeight);
            textAlign(RIGHT);
            fill(0f);
            final String label = (i == focusedCountryIndex ? "* ": "").concat(data.country.fullName);
            text(label, barChartLeftMargin - 5, barTop + (barHeight * (float)2/3));
        }
        textAlign(CENTER);
        final int barChartCenterX = barChartLeftMargin + barChartWidth / 2;
        text(selectedYear, barChartCenterX, barChartBottom + barChartBottomMargin * (float)2/3);
        text(focusedProperty.fullName, barChartCenterX, barChartBottom + barChartBottomMargin - 2);
        textAlign(RIGHT);
        text(formattedValue(maxValue), barChartLeftMargin + barChartWidth, barChartBottom + barChartBottomMargin * (float)1/3);
        textAlign(LEFT);
        text(0, barChartLeftMargin, barChartBottom + barChartBottomMargin * (float)1/3);
    }

    private String formattedValue(final float input) {
        if (input < 10) {
            return String.format("%.2f", input);
        }
        else if (input > 1000000) {
            return String.format("%.2f M", input / 1000000);
        }
        else if (input > 1000) {
            return String.format("%.2f K", input / 1000);
        }
        else {
            return String.format("%.0f", input);
        }
    }

    private void highlightSelectedCountries() {
        for (Country country: selectedCountries) {
            final PShape currentShape = world.getChild(country.shortCode);
            noStroke();
            for (BarChartData chartData: barChartDataList) {
                if (chartData.country.equals(country)) {
                    fill(chartData.color);
                }
            }
            shapeMap(currentShape);
        }
    }

    private List<BarChartData> getBarChartDataList() {
        final List<BarChartData> barChartDataList = new ArrayList<BarChartData>();
        try {
            final Field selectedField = CountryInfo.class.getField(focusedProperty.code);
            for (Country country: selectedCountries) {
                boolean added = false;
                for (CountryInfo countryInfo : infoList) {
                    if (countryInfo.year == selectedYear && countryInfo.country.equals(country.fullName)) {
                        barChartDataList.add(new BarChartData(country,
                                safeDouble(selectedField.getDouble(countryInfo))));
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    barChartDataList.add(new BarChartData(country, 0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return barChartDataList;
    }

    private final int MARGIN_TOP = 10;
    private final int MARGIN_BOTTOM = 50;
    private final int MARGIN_LEFT = 30;
    private final int MARGIN_RIGHT = 5;
    private final int AXIS_THICKNESS = 2;
    private final float DIAGRAM_RELATIVE_SIZE = (float)1/4;

    private void drawDiagram(final int noX, final int noY, final DiagramData data) {

        final String title = data.title;

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
        noStroke();
        rectMode(CORNER);
        rect(xAxisMin, yAxisTop, AXIS_THICKNESS, yAxisHeight);
        rect(xAxisMin, yAxisBottom, xAxisWidth, AXIS_THICKNESS);

        textAlign(CENTER);
        textSize(12);
        text(title, xAxisCenter, yAxisBottom + MARGIN_BOTTOM - 5);

        //for (String countieName : selectedCounties) {
            final int elementCount = data.elements.size();
            int[][] positions = new int[elementCount][2];  // x, y1, y2
            for (int i = 0; i < elementCount; i++) {
                float elemValue = safeFloat(data.elements.get(i).y);
                //final TableRow currentRow = getSelectedRowByIndex(i);
                positions[i][0] = (int) map(i, 0, elementCount - 1, xAxisMin, xAxisMax);
                positions[i][1] = (int) map(elemValue, 0, safeFloat(data.getMaxValue()), yAxisBottom, yAxisTop);
                //positions[i - rowIndexMin][2] = (int) map(getAbsoluteAffected(currentRow, countieName), 0, maxSelectedAbsolute, bottomDiagramYAxisMin, bottomDiagramYAxisMax);
            }
            //stroke(colorOf(countieName));
            stroke(127f);
            for (int i = 1; i < elementCount; i++) {
                line(positions[i - 1][0], positions[i - 1][1], positions[i][0], positions[i][1]);
                //line(positions[i - 1][0], positions[i - 1][2], positions[i][0], positions[i][2]);
            }
        //}
    }

    @Override
    public void keyPressed() {
        switch(key) {
            case 'w':
                if (focusedCountryIndex > 0) {
                    focusedCountryIndex--;
                }
                break;
            case 's':
                if (focusedCountryIndex < selectedCountries.size() - 1) {
                    focusedCountryIndex++;
                }
                break;
            case '-':
                if (selectedCountries.size() > 1) {
                    if (focusedCountryIndex == selectedCountries.size() - 1) {
                        focusedCountryIndex--;
                    }
                    selectedCountries.remove(focusedCountry);
                }
                break;
            case '+':
                if (selectedCountries.size() < 16) {
                    addNewSelectedCountry();
                }
                break;
            case 'e':
                if (selectedYear < MAX_YEAR) {
                    selectedYear++;
                    setFocusedCountryIndex(focusedCountry);
                }
                break;
            case 'q':
                if (selectedYear > MIN_YEAR) {
                    selectedYear--;
                    setFocusedCountryIndex(focusedCountry);
                }
                break;
            case 'a':
                replaceFocusedCountry(-1);
                break;
            case 'd':
                replaceFocusedCountry(1);
                break;
            case 'x':
                changeFocusedProperty(1);
                break;
            case 'y':
                changeFocusedProperty(-1);
                break;
        }
        redraw();
    }

    private void setFocusedCountry() {
        focusedCountry = barChartDataList.get(focusedCountryIndex).country;
    }

    private void addNewSelectedCountry() {
        for (Country country: Country.values()) {
            if (!selectedCountries.contains(country)) {
                selectedCountries.add(country);
                setFocusedCountryIndex(country);
                break;
            }
        }
    }

    private void replaceFocusedCountry(final int indexDiff) {
        selectedCountries.remove(focusedCountry);
        int indexToAdd = Arrays.asList(Country.values()).indexOf(focusedCountry);
        Country newCountry;
        do {
            indexToAdd += indexDiff;
            if (indexToAdd == Country.values().length) {
                indexToAdd = 0;
            }
            else if (indexToAdd == -1) {
                indexToAdd = Country.values().length - 1;
            }
            newCountry = Country.values()[indexToAdd];
        } while (selectedCountries.contains(newCountry));
        selectedCountries.add(newCountry);
        setFocusedCountryIndex(newCountry);
    }

    private void setFocusedCountryIndex(final Country country) {
        drawBarChart();
        for (BarChartData chartData: barChartDataList) {
            if (chartData.country.equals(country)) {
                focusedCountryIndex = barChartDataList.indexOf(chartData);
            }
        }
    }

    private void changeFocusedProperty(final int indexDiff) {
        int newIndex = focusedPropertyIndex + indexDiff;
        if (newIndex == Property.values().length) {
            newIndex = 0;
        }
        else if (newIndex == -1) {
            newIndex = Property.values().length - 1;
        }
        focusedPropertyIndex = newIndex;
        setFocusedProperty();
        setFocusedCountryIndex(focusedCountry);
    }

    private void setFocusedProperty() {
        focusedProperty = Property.values()[focusedPropertyIndex];
    }

    private static double safeDouble(final double input) {
        if (Double.isNaN(input)) {
            return 0f;
        }
        else {
            return input;
        }
    }

    private static float safeFloat(final double input) {
        if (Double.isNaN(input) || Double.isInfinite(input)) {
            return 0f;
        }
        else {
            return (float)input;
        }
    }

    private static class BarChartData implements Comparable<BarChartData> {
        public Country country;
        public Double value;
        public int color;

        @Override
        public int compareTo(BarChartData o) {
            return this.value.compareTo(o.value);
        }

        public BarChartData(final Country country, final double value) {
            this.country = country;
            this.value = value;
        }
    }

    private static class DiagramData {
        public String title;
        public final List<DiagramDataElement> elements = new ArrayList<DiagramDataElement>();

        public DiagramData(String title) {
            this.title = title;
        }

        public float getMaxValue() {
            double max = safeDouble(elements.get(0).y);
            for (DiagramDataElement element: elements) {
                final double value = safeDouble(element.y);
                if (value > max) {
                    max = value;
                }
            }
            return (float)max;
        }
    }

    private static class DiagramDataElement {
        public int x;
        public double y;
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

    private static enum Country {
        ABW("AW", "Aruba"),
        AFG("AF", "Afghanistan"),
        AGO("AO", "Angola"),
        AIA("AI", "Anguilla"),
        ALB("AL", "Albania"),
        AND("AD", "Andorra"),
        ANT("NL", "Netherlands Antilles"),
        ARE("AE", "United Arab Emirates"),
        ARG("AR", "Argentina"),
        ARM("AM", "Armenia"),
        ATG("AG", "Antigua and Barbuda"),
        AUS("AU", "Australia"),
        AUT("AT", "Austria"),
        AZE("AZ", "Azerbaijan"),
        BDI("BI", "Burundi"),
        BEL("BE", "Belgium"),
        BEN("BJ", "Benin"),
        BFA("BF", "Burkina Faso"),
        BGD("BD", "Bangladesh"),
        BGR("BG", "Bulgaria"),
        BHS("BS", "Bahamas"),
        BIH("BA", "Bosnia and Herzegovina"),
        BLR("BY", "Belarus"),
        BLZ("BZ", "Belize"),
        BMU("BM", "Bermuda"),
        BOL("BO", "Bolivia"),
        BRA("BR", "Brazil"),
        BRB("BB", "Barbados"),
        BRN("BN", "Brunei"),
        BTN("BT", "Bhutan"),
        BWA("BW", "Botswana"),
        CAF("CF", "Central African Republic"),
        CAN("CA", "Canada"),
        CHE("CH", "Switzerland"),
        CHL("CL", "Chile"),
        CHN("CN", "China"),
        CIV("CI", "Cote d'Ivoire"),
        CMR("CM", "Cameroon"),
        COD("CD", "Democratic Republic of Congo"),
        COG("CG", "Congo"),
        COL("CO", "Colombia"),
        COM("KM", "Comoros"),
        CPV("CV", "Cape Verde"),
        CRI("CR", "Costa Rica"),
        CUB("CU", "Cuba"),
        CUW("CW", "Curacao"),
        CYM("KY", "Cayman Islands"),
        CYP("CY", "Cyprus"),
        CZE("CZ", "Czechia"),
        DEU("DE", "Germany"),
        DJI("DJ", "Djibouti"),
        DMA("DM", "Dominica"),
        DNK("DK", "Denmark"),
        DZA("DZ", "Algeria"),
        ECU("EC", "Ecuador"),
        EGY("EG", "Egypt"),
        ERI("ER", "Eritrea"),
        ESP("ES", "Spain"),
        EST("EE", "Estonia"),
        ETH("ET", "Ethiopia"),
        FIN("FI", "Finland"),
        FJI("FJ", "Fiji"),
        FLK("FK", "Falkland Islands"),
        FRA("FR", "France"),
        FRO("FO", "Faeroe Islands"),
        GAB("GA", "Gabon"),
        GBR("GB", "United Kingdom"),
        GEO("GE", "Georgia"),
        GHA("GH", "Ghana"),
        GIN("GN", "Guinea"),
        GLP("GP", "Guadeloupe"),
        GMB("GM", "Gambia"),
        GNQ("GQ", "Equatorial Guinea"),
        GRC("GR", "Greece"),
        GRD("GD", "Grenada"),
        GRL("GL", "Greenland"),
        GTM("GT", "Guatemala"),
        GUF("GF", "French Guiana"),
        GUY("GY", "Guyana"),
        HKG("HK", "Hong Kong"),
        HND("HN", "Honduras"),
        HRV("HR", "Croatia"),
        HTI("HT", "Haiti"),
        HUN("HU", "Hungary"),
        IDN("ID", "Indonesia"),
        IND("IN", "India"),
        IRL("IE", "Ireland"),
        ISL("IS", "Iceland"),
        ISR("IL", "Israel"),
        ITA("IT", "Italy"),
        JAM("JM", "Jamaica"),
        JOR("JO", "Jordan"),
        JPN("JP", "Japan"),
        KAZ("KZ", "Kazakhstan"),
        KEN("KE", "Kenya"),
        KGZ("KG", "Kyrgyzstan"),
        KHM("KH", "Cambodia"),
        KNA("KN", "Saint Kitts and Nevis"),
        KOR("KR", "South Korea"),
        KWT("KW", "Kuwait"),
        LBN("LB", "Lebanon"),
        LBR("LR", "Liberia"),
        LBY("LY", "Libya"),
        LCA("LC", "Saint Lucia"),
        LIE("LI", "Liechtenstein"),
        LKA("LK", "Sri Lanka"),
        LSO("LS", "Lesotho"),
        LTU("LT", "Lithuania"),
        LUX("LU", "Luxembourg"),
        LVA("LV", "Latvia"),
        MAR("MA", "Morocco"),
        MDA("MD", "Moldova"),
        MDG("MG", "Madagascar"),
        MEX("MX", "Mexico"),
        MLI("ML", "Mali"),
        MMR("MM", "Myanmar"),
        MNE("ME", "Montenegro"),
        MNG("MN", "Mongolia"),
        MOZ("MZ", "Mozambique"),
        MRT("MR", "Mauritania"),
        MSR("MS", "Montserrat"),
        MTQ("MQ", "Martinique"),
        MUS("MU", "Mauritius"),
        NAM("NA", "Namibia"),
        NCL("NC", "New Caledonia"),
        NER("NE", "Niger"),
        NIC("NI", "Nicaragua"),
        NLD("NL", "Netherlands"),
        NOR("NO", "Norway"),
        NPL("NP", "Nepal"),
        NRU("NR", "Nauru"),
        NZL("NZ", "New Zealand"),
        OMN("OM", "Oman"),
        PAK("PK", "Pakistan"),
        PAN("PA", "Panama"),
        PER("PE", "Peru"),
        PHL("PH", "Philippines"),
        PNG("PG", "Papua New Guinea"),
        POL("PL", "Poland"),
        PRI("PR", "Puerto Rico"),
        PRK("KP", "North Korea"),
        PRT("PT", "Portugal"),
        PRY("PY", "Paraguay"),
        PSE("PS", "Palestine"),
        PYF("PF", "French Polynesia"),
        QAT("QA", "Qatar"),
        ROU("RO", "Romania"),
        RUS("RU", "Russia"),
        RWA("RW", "Rwanda"),
        SAU("SA", "Saudi Arabia"),
        SDN("SD", "Sudan"),
        SEN("SN", "Senegal"),
        SGP("SG", "Singapore"),
        SLB("SB", "Solomon Islands"),
        SLE("SL", "Sierra Leone"),
        SLV("SV", "El Salvador"),
        SOM("SO", "Somalia"),
        SRB("RS", "Serbia"),
        SSD("SS", "South Sudan"),
        STP("ST", "Sao Tome and Principe"),
        SUR("SR", "Suriname"),
        SVK("SK", "Slovakia"),
        SVN("SI", "Slovenia"),
        SWE("SE", "Sweden"),
        SXM("SX", "Sint Maarten (Dutch part)"),
        SYC("SC", "Seychelles"),
        SYR("SY", "Syria"),
        TCA("TC", "Turks and Caicos Islands"),
        TCD("TD", "Chad"),
        TGO("TG", "Togo"),
        THA("TH", "Thailand"),
        TJK("TJ", "Tajikistan"),
        TKM("TM", "Turkmenistan"),
        TLS("TL", "Timor"),
        TON("TO", "Tonga"),
        TTO("TT", "Trinidad and Tobago"),
        TUN("TN", "Tunisia"),
        TUR("TR", "Turkey"),
        TWN("TW", "Taiwan"),
        TZA("TZ", "Tanzania"),
        UGA("UG", "Uganda"),
        UKR("UA", "Ukraine"),
        URY("UY", "Uruguay"),
        USA("US", "United States"),
        UZB("UZ", "Uzbekistan"),
        VEN("VE", "Venezuela"),
        VGB("VG", "British Virgin Islands"),
        VNM("VN", "Vietnam"),
        VUT("VU", "Vanuatu"),
        YEM("YE", "Yemen"),
        ZAF("ZA", "South Africa"),
        ZMB("ZM", "Zambia"),
        ZWE("ZW", "Zimbabwe");

        Country(final String shortCode, final String fullName) {
            this.shortCode = shortCode;
            this.fullName = fullName;
        }

        String shortCode;
        String fullName;
    }

    private static enum Property {
        ENERGY_CONSUMPTION("primary_energy_consumption", "Primary energy consumption"),
        ENERGY_PCPT("energy_per_capita", "Energy per capita"),
        POPULATION("population", "Population"),
        ENERGY_PGDP("energy_per_gdp", "Energy per GDP"),
        NITROUS_OXIDE("nitrous_oxide", "Nitrous oxide"),
        NITROUS_OXIDE_PCPT("nitrous_oxide_per_capita", "Nitrous oxide per capita"),
        CO2("co2", "CO2"),
        CO2_PCPT("co2_per_capita", "CO2 per capita");

        Property(String code, String fullName) {
            this.code = code;
            this.fullName = fullName;
        }

        public String code;
        public String fullName;
    }

    public static void main(String[] args) {
        PApplet.main(Application2.class.getName());
    }
}
