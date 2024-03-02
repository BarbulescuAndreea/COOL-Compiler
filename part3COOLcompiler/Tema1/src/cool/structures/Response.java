package cool.structures;

import java.util.List;

public class Response {

    StatusCodes code;
    String message;

    List<String> arguments;

    public Response(StatusCodes code, List<String> arguments) {
        this.code = code;
        this.arguments = arguments;

        createMessage();
    }

    public void createMessage() {
        switch(code) {
            case OK:
                this.message = StatusCodes.OK.getMessage();
                break;

            // CLASS Specific
            case CLASS_NAMED_SELF_TYPE:
                this.message = "Class has illegal name SELF_TYPE";
                break;
            case CLASS_REDEFINED:
                this.message = "Class " + arguments.get(0) + " is redefined";
                break;
            case CLASS_WITH_ILLEGAL_PARENT:
                this.message = "Class " + arguments.get(0) + " has illegal parent " + arguments.get(1);
                break;
            case CLASS_WITH_UNDEFINED_PARENT:
                this.message = "Class " + arguments.get(0) + " has undefined parent " + arguments.get(1);
                break;
            case CLASS_CYCLIC_INHERITANCE:
                this.message = "Inheritance cycle for class " + arguments.get(0);
                break;

            // Attributes Specific
            case ATTRIBUTE_CALLED_SELF:
                this.message = "Class " + arguments.get(0) + " has attribute with illegal name self";
                break;
            case ATTRIBUTE_REDEFINED:
                this.message = "Class " + arguments.get(0) + " redefines attribute " + arguments.get(1);
                break;
            case ATTRIBUTE_REDEFINES_INHERITED:
                this.message = "Class " + arguments.get(0) + " redefines inherited attribute " + arguments.get(1);
                break;
            case ATTRIBUTE_WITH_UNDEFINED_TYPE:
                this.message = "Class " + arguments.get(0) + " has attribute " + arguments.get(1)
                                    + " with undefined type " + arguments.get(2);
                break;
            case ATTRIBUTE_AND_INITIALIZATION_DONT_MATCH:
                this.message = "Type " + arguments.get(0) + " of initialization expression of attribute " + arguments.get(1)
                        + " is incompatible with declared type " + arguments.get(2);
                break;

            // Functions Specific
            case FUNCTION_REDEFINED:
                this.message = "Class " + arguments.get(0) + " redefines method " + arguments.get(1);
                break;
            case FUNCTION_WITH_UNDEFINED_RETURN_TYPE:
                this.message = "Class " + arguments.get(0) + " has method " + arguments.get(1) + " with undefined return type " + arguments.get(2);
                break;
            case FUNCTION_OVERRIDE_DIFFERENT_NR_OF_PARAMS:
                this.message = "Class " + arguments.get(0) + " overrides method " + arguments.get(1) + " with different number of formal parameters";
                break;
            case FUNCTION_OVERRIDE_CHANGE_FORMAL_TYPE:
                this.message = "Class " + arguments.get(0) + " overrides method " + arguments.get(1) + " but changes type of formal parameter "
                                    + arguments.get(2) + " from " + arguments.get(3) + " to " + arguments.get(4);
                break;
            case FUNCTION_OVERRIDE_CHANGE_RETURN_TYPE:
                this.message = "Class " + arguments.get(0) + " overrides method " + arguments.get(1) + " but changes return type from "
                                    + arguments.get(2) + " to " + arguments.get(3);
                break;
            case FUNCTION_RETURN_AND_BODY_DONT_MATCH:
                this.message = "Type " + arguments.get(0) + " of the body of method " + arguments.get(1) + " is incompatible with declared return type "
                                    + arguments.get(2);
                break;

            // Formal Params of Functions
            case FORMAL_PARAM_CALLED_SELF:
                this.message = "Method " + arguments.get(0) + " of class " +  arguments.get(1)
                        + " has formal parameter with illegal name self";
                break;
            case FORMAL_PARAM_REDEFINED:
                this.message = "Method " + arguments.get(0) + " of class " +  arguments.get(1)
                                + " redefines formal parameter " + arguments.get(2);
                break;
            case FORMAL_PARAM_TYPE_SELF_TYPE:
                this.message = "Method " + arguments.get(0) + " of class " + arguments.get(1) +
                                " has formal parameter " + arguments.get(2) + " with illegal type SELF_TYPE";
                break;
            case FORMAL_PARAM_UNDEFINED_TYPE:
                this.message = "Method " + arguments.get(0) + " of class " +  arguments.get(1)
                        + " has formal parameter " + arguments.get(2)
                        + " with undefined type " + arguments.get(3);
                break;

            // LET
            case LET_VARIABLE_NAMED_SELF:
                this.message = "Let variable has illegal name self";
                break;
            case LET_VARIABLES_UNDEFINED_TYPE:
                this.message = "Let variable " + arguments.get(0) + " has undefined type " + arguments.get(1);
                break;
            case LET_INITIALIZATION_AND_TYPE_DONT_MATCH:
                this.message = "Type " + arguments.get(0) + " of initialization expression of identifier "
                                + arguments.get(1) + " is incompatible with declared type " + arguments.get(2);
                break;

            // CASE
            case CASE_VARIABLE_NAMED_SELF:
                this.message = "Case variable has illegal name self";
                break;
            case CASE_VARIABLE_TYPE_SELF_TYPE:
                this.message = "Case variable " + arguments.get(0) + " has illegal type SELF_TYPE";
                break;
            case CASE_VARIABLE_UNDEFINED_TYPE:
                this.message = "Case variable " + arguments.get(0) + " has undefined type " + arguments.get(1);
                break;

            // Variables ID
            case UNDEFINED_IDENTIFIER:
                this.message = "Undefined identifier " + arguments.get(0);
                break;

            // Operators
            case OPERATOR_OPERANDS_ARENT_INT:
                this.message = "Operand of " + arguments.get(0) + " has type " + arguments.get(1) + " instead of Int";
                break;
            case OPERATOR_CANT_COMPARE:
                this.message = "Cannot compare " + arguments.get(0) + " with " + arguments.get(1);
                break;
            case OPERATOR_OPERAND_ISNT_BOOL:
                this.message = "Operand of not has type " + arguments.get(0) + " instead of Bool";
                break;

            // Assign
            case ASSIGN_TO_SELF:
                this.message = "Cannot assign to self";
                break;
            case ASSIGN_ASSIGNED_AND_DECLARED_TYPES_DONT_MATCH:
                this.message = "Type " + arguments.get(0) + " of assigned expression is incompatible with declared type "
                                    + arguments.get(1) + " of identifier " + arguments.get(2);
                break;

            // NEW
            case NEW_UNDEFINED_TYPE:
                this.message = "new is used with undefined type " + arguments.get(0);
                break;

            // WHILE & IF
            case WHILE_COND_NOT_BOOL:
                this.message = "While condition has type " + arguments.get(0) + " instead of Bool";
                break;
            case IF_COND_NOT_BOOL:
                this.message = "If condition has type " + arguments.get(0) + " instead of Bool";
                break;

            // Dispatch
            case DISPATCH_METHOD_UNDEFINED:
                this.message = "Undefined method " + arguments.get(0) + " in class " + arguments.get(1);
                break;
            case DISPATCH_WRONG_NUMBER_OF_ARGUMENTS:
                this.message = "Method " + arguments.get(0) + " of class " + arguments.get(1)
                                + " is applied to wrong number of arguments";
                break;
            case DISPATCH_ACTUAL_PARAM_WRONG_TYPE:
                this.message = "In call to method " + arguments.get(0) + " of class " + arguments.get(1)
                        + ", actual type " + arguments.get(2) + " of formal parameter " + arguments.get(3)
                        + " is incompatible with declared type " + arguments.get(4);
                break;
            case DISPATCH_ON_SELF_TYPE:
                this.message = "Type of static dispatch cannot be SELF_TYPE";
                break;
            case DISPATCH_ON_UNDEFINED_TYPE:
                this.message = "Type " + arguments.get(0) + " of static dispatch is undefined";
                break;
            case DISPATCH_ON_UNRELATED_CLASS:
                this.message = "Type " + arguments.get(0) +  " of static dispatch is not a superclass of type "
                                    + arguments.get(1);
                break;
        }

    }

    public String getMessage() {
        return message;
    }

    public StatusCodes getCode() {
        return code;
    }
}




