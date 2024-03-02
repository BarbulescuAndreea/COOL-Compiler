package cool.structures;

import javax.swing.plaf.PanelUI;
import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

    public static final String SELF_TYPE = "SELF_TYPE";
    public static final String Object = "Object";
    public static final String IO = "IO";
    public static final String INT = "Int";
    public static final String STRING = "String";
    public static final String BOOL = "Bool";
    public static final ArrayList<String> ILLEGAL_EXTENDS = new ArrayList<>(Arrays.asList(INT, STRING, BOOL, SELF_TYPE));


    public static final String SELF = "self";
    public static final String TILDE = "~";
    public static final ArrayList<String> COMPARABLE = new ArrayList<>(Arrays.asList(INT, STRING, BOOL));

}
