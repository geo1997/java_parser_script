import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    static class JavaFileInfo {
        public String filePath;
        public List<String> details;

        public JavaFileInfo(String filePath) {
            this.filePath = filePath;
            this.details = new ArrayList<>();
        }

        public void addDetail(String detail) {
            this.details.add(detail);
        }
    }

    public static void main(String[] args) throws Exception {
        String jsonFilePath = "src/JSON/filePath.json"; // JSON file path
        ObjectMapper objectMapper = new ObjectMapper();

        // Read JSON file and extract file paths
        Map<?, ?> jsonMap = objectMapper.readValue(new File(jsonFilePath), Map.class);
        List<String> filePaths = (List<String>) jsonMap.get("filePaths");

        List<JavaFileInfo> fileInfoList = new ArrayList<>();

        // Process each file path
        for (String filePath : filePaths) {
            System.out.println("Processing file: " + filePath); // Print file path to console
            JavaFileInfo fileInfo = new JavaFileInfo(filePath);
            fileInfoList.add(fileInfo);

            CompilationUnit compilationUnit = StaticJavaParser.parse(new File(filePath));
            extractAndPrintInfo(compilationUnit, fileInfo);
        }

        // Write results to a JSON file with pretty print
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), fileInfoList);
    }

    private static void extractAndPrintInfo(CompilationUnit compilationUnit, JavaFileInfo fileInfo) {
        List<TypeDeclaration<?>> types = compilationUnit.getTypes();
        for (TypeDeclaration<?> type : types) {
            // Process class or interface
            String classVisibility = extractVisibility(type);
            String className = type.getNameAsString();
            String classDetail = String.format("%-10s %-20s %s", classVisibility, "Class", className);
            fileInfo.addDetail(classDetail);
            System.out.println(classDetail); // Print to console

            // Process methods
            for (MethodDeclaration method : type.getMethods()) {
                String methodVisibility = extractVisibility(method);
                String methodSignature = method.getSignature().asString();
                String methodDetail = String.format("%-10s %-20s %s", methodVisibility, "Method", methodSignature);
                fileInfo.addDetail(methodDetail);
                System.out.println(methodDetail); // Print to console
            }

            // Process fields
            for (FieldDeclaration field : type.getFields()) {
                String fieldVisibility = extractVisibility(field);
                String fieldSignature = field.getVariables().stream()
                        .map(v -> v.getNameAsString())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Unnamed");
                String fieldDetail = String.format("%-10s %-20s %s", fieldVisibility, "Variable", fieldSignature);
                fileInfo.addDetail(fieldDetail);
                System.out.println(fieldDetail); // Print to console
            }

            // Process constructors, if required
            for (ConstructorDeclaration constructor : type.getConstructors()) {
                String constructorVisibility = extractVisibility(constructor);
                String constructorSignature = constructor.getSignature().asString();
                String constructorDetail = String.format("%-10s %-20s %s", constructorVisibility, "Constructor", constructorSignature);
                fileInfo.addDetail(constructorDetail);
                System.out.println(constructorDetail); // Print to console
            }

            // Process inner classes, if required
            for (BodyDeclaration<?> member : type.getMembers()) {
                if (member instanceof ClassOrInterfaceDeclaration) {
                    ClassOrInterfaceDeclaration innerClass = (ClassOrInterfaceDeclaration) member;
                    String innerClassVisibility = extractVisibility(innerClass);
                    String innerClassName = innerClass.getNameAsString();
                    String innerClassDetail = String.format("%-10s %-20s %s", innerClassVisibility, "Inner Class", innerClassName);
                    fileInfo.addDetail(innerClassDetail);
                    System.out.println(innerClassDetail); // Print to console
                }
            }
        }
    }

    private static String extractVisibility(BodyDeclaration<?> declaration) {
        String visibility = "other";

        if (declaration instanceof NodeWithModifiers) {
            NodeWithModifiers<?> nodeWithModifiers = (NodeWithModifiers<?>) declaration;
            if (nodeWithModifiers.hasModifier(Modifier.Keyword.PUBLIC)) {
                visibility = "public";
            } else if (nodeWithModifiers.hasModifier(Modifier.Keyword.PRIVATE)) {
                visibility = "private";
            }
        }

        return visibility;
    }


}
