package cool.compiler;
import cool.compiler.consts.BoolConst;
import cool.compiler.consts.IntConst;
import cool.compiler.consts.StringConst;
import cool.structures.ClassSymbol;
import cool.structures.DefaultScope;
import cool.structures.Symbol;
import cool.structures.SymbolTable;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import javax.print.DocFlavor;
import java.util.*;

public class CodeGenVisitor implements ASTVisitor<ST> {
    static Set<String> notAllowed = new HashSet<>();

    LinkedHashMap<String, Symbol> orderedFiltered = new LinkedHashMap<>();
    STGroupFile templates = new STGroupFile("cool/compiler/cgen.stg");
    List<IntConst> listOfConstInt = new LinkedList<>();
    List<ST> intTemplates = new LinkedList<>();
    List<BoolConst> listOfConstBool = new LinkedList<>();

    List<StringConst> listOfConstString = new LinkedList<>();

    List<ST> stringTemplates = new LinkedList<>();

    private void createIntConst(){
        IntConst zeroConst = new IntConst(0, "int_const" + (listOfConstInt.size()));
        listOfConstInt.add(zeroConst);

        ST templateZero = templates.getInstanceOf("int_const");
        templateZero.add("index", listOfConstInt.size() - 1);
        templateZero.add("value", 0);

        intTemplates.add(templateZero);

        Map<String, Symbol> globalScope = ((DefaultScope)SymbolTable.globals).getSymbols();
        for (Map.Entry<String, Symbol> entry : globalScope.entrySet()) {
            String variableName = entry.getKey();
            Integer stringLen = variableName.length();
            boolean containsValue = listOfConstInt.stream().anyMatch(intConst -> intConst.value == stringLen);
            if (!containsValue) {
                IntConst intConst = new IntConst(stringLen, "int_const" + (listOfConstInt.size()));
                listOfConstInt.add(intConst);

                ST template = templates.getInstanceOf("int_const");
                template.add("index", listOfConstInt.size() - 1);
                template.add("value", stringLen);

                intTemplates.add(template);
            }
        }
    }

    void createStringConst(){
        StringConst emptyString = new StringConst("",5, "str_const" + listOfConstString.size());
        listOfConstString.add(emptyString);

        IntConst result = listOfConstInt.stream()
                .filter(intConst -> intConst.value == 0).toList().get(0);

        ST templateZero = templates.getInstanceOf("str_const");
        templateZero.add("index", listOfConstString.size() - 1);
        templateZero.add("length", 5);
        templateZero.add("value", result.string);
        templateZero.add("string", "");

        stringTemplates.add(templateZero);

        Map<String, Symbol> globalScope = ((DefaultScope)SymbolTable.globals).getSymbols();
        for (Map.Entry<String, Symbol> entry : orderedFiltered.entrySet()) {
            String variableName = entry.getKey();
            IntConst resultInt = listOfConstInt.stream()
                    .filter(intConst -> intConst.value == variableName.length()).toList().get(0);

            StringConst stringConst;
            if(variableName.equals("String")
            || variableName.equals("Bool")
            || variableName.equals("Object")
            || variableName.equals("Main")) {
                stringConst = new StringConst(variableName, 6, "str_const" + listOfConstString.size());
            }
            else{
                stringConst = new StringConst(variableName, 5, "str_const" + listOfConstString.size());
            }
            listOfConstString.add(stringConst);

            ST template = templates.getInstanceOf("str_const");
            template.add("index", listOfConstString.size() - 1);
            template.add("length", stringConst.length);
            template.add("value", resultInt.string);
            template.add("string", stringConst.value);

            stringTemplates.add(template);
        }
    }

    private ST createBoolConst(int index, int value) {
        BoolConst boolConst = new BoolConst(value);
        listOfConstBool.add(boolConst);

        // Create a template for bool_const section
        ST template = templates.getInstanceOf("bool_const");
        template.add("index", index);
        template.add("value", value);

        return template;
    }

    private ST initCode() {
        ST template = new ST(
                "    .data\n" +
                        "    .align  2\n" +
                        "    .globl  class_nameTab\n" +
                        "    .globl  Int_protObj\n" +
                        "    .globl  String_protObj\n" +
                        "    .globl  bool_const0\n" +
                        "    .globl  bool_const1\n" +
                        "    .globl  Main_protObj\n" +
                        "    .globl  _int_tag\n" +
                        "    .globl  _string_tag\n" +
                        "    .globl  _bool_tag\n" +
                        "_int_tag:\n" +
                        "    .word   2\n" +
                        "_string_tag:\n" +
                        "    .word   3\n" +
                        "_bool_tag:\n" +
                        "    .word   4\n"
        );
        return template;
    }

    private ST class_nameTab(){
        String templateString = "class_nameTab:\n";
        int index = 0;
        for(StringConst stringConst : listOfConstString){
            if(index == 0){
                index++;
                continue;
            }
            templateString += "    .word   " + stringConst.string + "\n";
        }
        ST template = new ST(templateString);
        return template;
    }

    public static Map<String, Symbol> filterMapByNames(Map<String, Symbol> inputMap, Set<String> namesToKeep) {
        Map<String, Symbol> filteredMap = new HashMap<>();

        for (Map.Entry<String, Symbol> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Symbol value = entry.getValue();

            if (!namesToKeep.contains(key)) {
                filteredMap.put(key, value);
            }
        }

        return filteredMap;
    }

    private ST class_objTab(){
        String templateString = "class_objTab:\n";
        int index = 0;
        for (Map.Entry<String, Symbol> entry : orderedFiltered.entrySet()) {
            templateString += "    .word   " + entry.getKey() + "_protObj\n";
            templateString += "    .word   " + entry.getKey() + "_init\n";
        }
        ST template = new ST(templateString);
        return template;
    }

    private ST create_default_prot(){
        ST template = new ST(
                "Int_protObj:\n" +
                        "    .word   2\n" +
                        "    .word   4\n" +
                        "    .word   Int_dispTab\n" +
                        "    .word   0\n" +
                        "String_protObj:\n" +
                        "    .word   3\n" +
                        "    .word   5\n" +
                        "    .word   String_dispTab\n" +
                        "    .word   int_const0\n" +
                        "    .asciiz \"\"\n" +
                        "    .align  2\n" +
                        "Bool_protObj:\n" +
                        "    .word   4\n" +
                        "    .word   4\n" +
                        "    .word   Bool_dispTab\n" +
                        "    .word   0\n"
        );
        return template;
    }

    private ST create_prot(String value, Integer index){
        ST template = templates.getInstanceOf("_protObj");
        template.add("index", index);
        template.add("value", value);
        return template;
    }

    List<String> buildMethod(ClassSymbol classSymbol){
        List<String> list = new LinkedList<>();
        for(Map.Entry<String, Symbol> entry : classSymbol.getMethods().entrySet()){
            //System.out.println("Class " + classSymbol.getName() + " " + entry.getKey());
            list.add("\t.word   " + classSymbol.getName() + "." + entry.getKey() + "\n");
        }
        Collections.reverse(list);
        return list;
    }

    private ST create_disp(String className, ClassSymbol symbol){
        String temp = className + "_dispTab:\n";
        ClassSymbol currentClass = symbol;
        List<String> allMethods = new LinkedList<>();

        while(currentClass != null){
            List<String> list = buildMethod(currentClass);
            allMethods.addAll(list);

            currentClass = currentClass.getInheritedClassSymbol();
        }

        Collections.reverse(allMethods);

        for(String line : allMethods){
            temp += line;
        }

        return new ST(temp);
    }

    private ST create_heap(){
        ST template = new ST(
                "    .globl  heap_start\n" +
                "heap_start:\n" +
                "    .word   0\n" +
                "    .text\n" +
                "    .globl  Int_init\n" +
                "    .globl  String_init\n" +
                "    .globl  Bool_init\n" +
                "    .globl  Main_init\n" +
                "    .globl  Main.main"
        );
        return template;
    }

    private ST create_init(String value, String value2){
        ST template = templates.getInstanceOf("directive_name");
        template.add("value", value);
        template.add("value2", value2);
        return template;
    }

    void init(){
        notAllowed.add("Object");
        notAllowed.add("IO");
        notAllowed.add("Int");
        notAllowed.add("String");
        notAllowed.add("Bool");
    }

    class TreeNode {
        private ClassSymbol data;
        private List<TreeNode> children;

        public TreeNode(ClassSymbol data) {
            this.data = data;
            this.children = new ArrayList<>();
        }

        public ClassSymbol getData() {
            return data;
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        public void addChild(TreeNode child) {
            children.add(child);
        }
    }
    public List<Symbol> sortByParentRelationship(Map<String, Symbol> symbolMap) {
        List<Symbol> sortedSymbols = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        // Cozi pentru parcurgerea în lățime
        Queue<TreeNode> queue = new LinkedList<>();

        // Adăugăm rădăcina (clasa care nu are părinte) în coadă
        for (Symbol symbol : symbolMap.values()) {
            if (((ClassSymbol)symbol).getInheritedClassSymbol() != null) {
                TreeNode root = new TreeNode((ClassSymbol) symbol);
                queue.add(root);
                visited.add(((ClassSymbol)symbol).getName());

                buildTree(root, symbolMap);
            }
        }

        // Parcurgerea în preordine
        traversePreOrder(queue.peek(), sortedSymbols);

        return sortedSymbols;
    }

    private void buildTree(TreeNode parent, Map<String, Symbol> symbolMap) {
        ClassSymbol parentSymbol = parent.getData();
        for (Symbol symbol : symbolMap.values()) {
            if (parentSymbol.getName().equals(((ClassSymbol)symbol).getInheritedClassName())) {
                TreeNode child = new TreeNode((ClassSymbol) symbol);
                parent.addChild(child);
                buildTree(child, symbolMap);
            }
        }
    }

    private static void traversePreOrder(TreeNode root, List<Symbol> result) {
        if (root != null) {
            result.add(root.getData());
            for (TreeNode child : root.getChildren()) {
                traversePreOrder(child, result);
            }
        }
    }

    private static List<ClassSymbol> getChildren(ClassSymbol parent, Map<String, Symbol> symbolMap) {
        List<ClassSymbol> children = new ArrayList<>();
        for (Symbol symbol : symbolMap.values()) {
            if (parent.getName().equals(((ClassSymbol)symbol).getInheritedClassName())) {
                children.add((ClassSymbol)symbol);
                System.out.printf("Found child: %s for parent: %s%n", symbol.getName(), parent.getName());
            }
        }
        return children;
    }

    @Override
    public ST visit(Program program) {
        init();
        ST template = new ST("");
        System.out.println(initCode().render());

        Map<String, Symbol> filtered = filterMapByNames(((DefaultScope)SymbolTable.globals).getSymbols(), notAllowed);

        List<Symbol> list = sortByParentRelationship(filtered);

        for(Map.Entry<String, Symbol> entry : ((DefaultScope)SymbolTable.globals).getSymbols().entrySet()){
            if(notAllowed.contains(entry.getKey())){
                orderedFiltered.put(entry.getKey(), entry.getValue());
            }
        }

        for (Symbol symbol : list) {
            orderedFiltered.put(symbol.getName(), symbol);
        }

        createIntConst();
        createStringConst();

        for (ST stringTemplate : stringTemplates) {
            System.out.println(stringTemplate.render());
        }

        for (ST intTemplate : intTemplates) {
            System.out.println(intTemplate.render());
        }

        System.out.println(createBoolConst(0, 0).render());
        System.out.println(createBoolConst(1, 1).render());

        System.out.println(class_nameTab().render());
        System.out.println(class_objTab().render());

        int index = 0;
        for (Map.Entry<String, Symbol> entry : orderedFiltered.entrySet()) {
            if(entry.getKey().equals("String") || entry.getKey().equals("Int") || entry.getKey().equals("Bool")){
                if(index == 2){
                    System.out.println(create_default_prot().render());
                }
                index++;
            }
            else{
                System.out.println(create_prot(entry.getKey(), index).render());
                index++;
            }
        }

        for (Map.Entry<String, Symbol> entry : orderedFiltered.entrySet()) {
            System.out.println(create_disp(entry.getKey(), (ClassSymbol) entry.getValue()).render());
        }

        System.out.println(create_heap().render());

        for (Map.Entry<String, Symbol> entry : ((DefaultScope)SymbolTable.globals).getSymbols().entrySet()) {
            if(entry.getKey().equals("Object")){
                ST templateObject = new ST("Object_init:\n" +
                        "    addiu   $sp $sp -12\n" +
                        "    sw      $fp 12($sp)\n" +
                        "    sw      $s0 8($sp)\n" +
                        "    sw      $ra 4($sp)\n" +
                        "    addiu   $fp $sp 4\n" +
                        "    move    $s0 $a0\n" +
                        "    move    $a0 $s0\n" +
                        "    lw      $fp 12($sp)\n" +
                        "    lw      $s0 8($sp)\n" +
                        "    lw      $ra 4($sp)\n" +
                        "    addiu   $sp $sp 12\n" +
                        "    jr      $ra");
                System.out.println(templateObject.render());
            }
            else {
                System.out.println(create_init(entry.getKey(), ((ClassSymbol) entry.getValue()).getInheritedClassName()).render());
            }
        }
        for(Expression clazz : program.classes){
            clazz.accept(this);
        }

        return template;
    }

    @Override
    public ST visit(Clazz clazz) {
//        ST template = templates.getInstanceOf("class");
//        template.add("classname", clazz.id.getText());
//        if (clazz.inheritedType != null)
//            template.add("parent", clazz.inheritedType.getText());
//        for (Expression feature : clazz.features) {
//            template.add("features", feature.accept(this));
//        }
//        return template;
        for(Expression feature : clazz.features){
            feature.accept(this);
        }

        return null;
    }

    @Override
    public ST visit(AttributeDefinition attributeDefinition) {
//        ST template = new ST("<feature(attr_name, class_name)>");
//        template.add("attr_name", attributeDefinition.token.getText());
//        template.add("class_name", attributeDefinition.type.getText());
//        return template;
        return null;
    }

    @Override
    public ST visit(FunctionDefinition functionDefinition) {
//        ST template = new ST("<feature(method_name, class_name, formal_params, locals, expr)>");
//        template.add("method_name", functionDefinition.token.getText());
//        template.add("class_name", functionDefinition.type.getText());
//        // Add other parameters, locals, and expressions
//        return template;
        ST template = templates.getInstanceOf("method");
        template.add("class", ((ClassSymbol)functionDefinition.scope).getName());
        template.add("method", functionDefinition.functionSymbol.getName());
        System.out.println(template.render());
        return null;
    }

    @Override
    public ST visit(FormalDefinition formal) {
        return null;
    }

    @Override
    public ST visit(IsVoid isVoid) {
        return null;
    }

    @Override
    public ST visit(NewExpression newExpression) {
        return null;
    }

    @Override
    public ST visit(Id id) {
        return null;
    }

    @Override
    public ST visit(Assign assign) {
        return null;
    }

    @Override
    public ST visit(Dispatch dispatch) {
        return null;
    }

    @Override
    public ST visit(Int intt) {
        return null;
    }

    @Override
    public ST visit(Bool bool) {
        return null;
    }

    @Override
    public ST visit(Stringg stringg) {
        return null;
    }

    @Override
    public ST visit(ArithmeticOperation operation) {
        return null;
    }

    @Override
    public ST visit(TildeOperation tildeOperation) {
        return null;
    }

    @Override
    public ST visit(ParenthesisOperation operation) {
        return null;
    }

    @Override
    public ST visit(RelationalOperation relationalOperation) {
        return null;
    }

    @Override
    public ST visit(Not not) {
        return null;
    }

    @Override
    public ST visit(Iff iff) {
        return null;
    }

    @Override
    public ST visit(While whilee) {
        return null;
    }

    @Override
    public ST visit(Let let) {
        return null;
    }

    @Override
    public ST visit(LetItem letItem) {
        return null;
    }

    @Override
    public ST visit(Case casee) {
        return null;
    }

    @Override
    public ST visit(CaseItem caseItem) {
        return null;
    }

    // Implement other visit methods for remaining AST nodes

    // ...

    @Override
    public ST visit(Block block) {
        ST template = new ST(".text\n" +
                ".globl  <block_function_name>\n" +
                "<block_function_name>:\n" +
                "    # ... (code for block)\n");
        // Add code for the block based on its contents
        return template;
    }

    // Add other visit methods for remaining AST nodes

    // ...

}
