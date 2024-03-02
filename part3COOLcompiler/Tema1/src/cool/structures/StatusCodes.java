package cool.structures;

public enum StatusCodes {
    OK("Ok"),

    // class
    CLASS_NAMED_SELF_TYPE("Class has illegal name SELF_TYPE"),
    CLASS_REDEFINED("Class is redefined"),
    CLASS_WITH_ILLEGAL_PARENT("Class has illegal parent"),
    CLASS_WITH_UNDEFINED_PARENT("Class has undefined parent"),
    CLASS_CYCLIC_INHERITANCE("Inheritance cycle for class"),


    // Attributes
    ATTRIBUTE_CALLED_SELF("Class has attribute with illegal name self"),
    ATTRIBUTE_REDEFINED("Class redefines attribute "),
    ATTRIBUTE_REDEFINES_INHERITED("Class redefines inherited attribute"),
    ATTRIBUTE_WITH_UNDEFINED_TYPE("Class has attribute <a> with undefined type "),
    ATTRIBUTE_AND_INITIALIZATION_DONT_MATCH("Type <T1> of initialization expression of attribute <a> is\n" +
            "incompatible with declared type <T2>"),

    // function
    FUNCTION_REDEFINED("Class <C> redefines method <m>"),
    FUNCTION_WITH_UNDEFINED_RETURN_TYPE("Class <C> has method <m> with undefined return type <T>"),
    FUNCTION_OVERRIDE_DIFFERENT_NR_OF_PARAMS("Class <C> overrides method <m> with different number of\n" +
            "formal parameters"),
    FUNCTION_OVERRIDE_CHANGE_FORMAL_TYPE("Class <C> overrides method <m> but changes type of formal\n" +
            "parameter <f> from <T1> to <T2>"),
    FUNCTION_OVERRIDE_CHANGE_RETURN_TYPE("Class <C> overrides method <m> but changes return type from\n" +
            "<T1> to <T2>"),
    FUNCTION_RETURN_AND_BODY_DONT_MATCH("Type <T1> of the body of method <m> is incompatible with\n" +
            "declared return type <T2>"),

    // Formal Params of Function
    FORMAL_PARAM_CALLED_SELF("Method <m> of class <C> has formal parameter with illegal name self"),
    FORMAL_PARAM_REDEFINED("Method <m> of class <C> redefines formal parameter <f>"),
    FORMAL_PARAM_TYPE_SELF_TYPE("Method <m> of class <C> has formal parameter <f> with illegal type SELF_TYPE"),
    FORMAL_PARAM_UNDEFINED_TYPE("Method <m> of class <C> has formal parameter <f> with\n" +
            "undefined type <T>"),

    // LET
    LET_VARIABLE_NAMED_SELF("Let variable has illegal name self"),
    LET_VARIABLES_UNDEFINED_TYPE("Let variable <l> has undefined type <T>"),
    LET_INITIALIZATION_AND_TYPE_DONT_MATCH("Type <T1> of initialization expression of identifier <i> is\n" +
            "incompatible with declared type <T2>"),

    // CASE
    CASE_VARIABLE_NAMED_SELF("Case variable has illegal name sel"),
    CASE_VARIABLE_TYPE_SELF_TYPE("Case variable <c> has illegal type SELF_TYPE"),
    CASE_VARIABLE_UNDEFINED_TYPE("Case variable <c> has undefined type <T>"),

    // Variables ID
    UNDEFINED_IDENTIFIER("Undefined identifier <i>"),


    // Operators
    OPERATOR_OPERANDS_ARENT_INT("Operand of <op> has type <T> instead of Int"),
    OPERATOR_CANT_COMPARE(" Cannot compare <T1> with <T2>"),
    OPERATOR_OPERAND_ISNT_BOOL("Operand of not has type <T> instead of Bool"),

    // Assign
    ASSIGN_TO_SELF("Cannot assign to self"),
    ASSIGN_ASSIGNED_AND_DECLARED_TYPES_DONT_MATCH("Type <T1> of assigned expression is incompatible with\n" +
            "declared type <T2> of identifier <i>"),

    // NEW
    NEW_UNDEFINED_TYPE("new is used with undefined type <T>"),

    // WHILE & IF
    WHILE_COND_NOT_BOOL("While condition has type <T> instead of Bool"),
    IF_COND_NOT_BOOL("If condition has type <T> instead of Bool"),

    // Dispatch
    DISPATCH_METHOD_UNDEFINED("Undefined method <m> in class <C>"),
    DISPATCH_WRONG_NUMBER_OF_ARGUMENTS("Method <m> of class <C> is applied to wrong number of arguments"),
    DISPATCH_ACTUAL_PARAM_WRONG_TYPE("In call to method <m> of class <C>, actual type <T1> of formal parameter <f> is incompatible with declared type <T2>"),
    DISPATCH_ON_SELF_TYPE("Type of static dispatch cannot be SELF_TYPE"),
    DISPATCH_ON_UNDEFINED_TYPE("Type <T> of static dispatch is undefined"),
    DISPATCH_ON_UNRELATED_CLASS("Type <T1> of static dispatch is not a superclass of type <T2>");

    String message;

    StatusCodes(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
