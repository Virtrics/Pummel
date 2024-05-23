package org.virtrics;

import de.elnarion.util.plantuml.generator.classdiagram.PlantUMLClassDiagramGenerator;
import de.elnarion.util.plantuml.generator.classdiagram.config.PlantUMLClassDiagramConfigBuilder;
import de.elnarion.util.plantuml.generator.sequencediagram.PlantUMLSequenceDiagramGenerator;
import de.elnarion.util.plantuml.generator.sequencediagram.config.PlantUMLSequenceDiagramConfigBuilder;
import de.elnarion.util.plantuml.generator.sequencediagram.exception.NotFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class PlantUMLDiagramService {

    private static final String classExclusionList = ".*(test|base|springframework|jakarta|apache|lombok|gson|jwt|slf4j|mockito|build|Builder|BaseRestClient).*";
    private static final String methodExclusionList = ".*(toString|equals|void|hashCode|clone|finalize|wait|notify|build|Headers|notifyAll).*";
    public static boolean squashSubclasses = false;

    public void generateSequenceDiagram(String tag, final Class clazz, final String methodName) throws IOException, NotFoundException {
        generateSequenceDiagram(tag, clazz, methodName, classExclusionList, methodExclusionList);
    }

    public void generateSequenceDiagram(String tag, final Class clazz, final String methodName, String paramClassBlacklistRegexp, String methodExclusionList) throws IOException, NotFoundException {
        PlantUMLSequenceDiagramConfigBuilder builder = new PlantUMLSequenceDiagramConfigBuilder(clazz.getName(), methodName)
                .withUseShortClassName(true)
                .withShowReturnTypes(true)
                .withIgnoreStandardClasses(true)
                .withClassBlacklistRegexp(paramClassBlacklistRegexp)
                .withMethodBlacklistRegexp(methodExclusionList);


        PlantUMLSequenceDiagramGenerator generator = new PlantUMLSequenceDiagramGenerator(builder.build());

        String generatedDiagram = generator.generateDiagramText();
        generatedDiagram = cleanup(generatedDiagram, clazz.getSimpleName());
        writeToFile("docs/" + clazz.getSimpleName() + "_" + methodName + "_" + tag + "_SD.puml", generatedDiagram);
    }

    private String cleanup(String generatedDiagram, String origin) {

        String tmp = generatedDiagram.trim().replaceAll("\\r", "").replaceAll("\\$", "_")
                .replaceAll("participant " + origin, "actor " + origin + " #green");

        String[] participants = Arrays.stream(tmp.split("\n")).filter(s -> (s.contains("participant") && (s.contains("_")))).toArray(String[]::new);
        if (squashSubclasses) {
            Map<String, String> map = new HashMap<>();
            for (String p : participants) {

                String[] s = p.split("_| ");
                String[] r = p.split(" ");
                map.put(r[1].trim(), s[1].trim());
            }

            for (Entry<String, String> stringStringEntry : map.entrySet()) {
                tmp = tmp.replaceAll(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }

        return tmp;
    }

    public void generateClassDiagram(final String filename, String tag, List<String> scanPackages, List<String> hideClasses) throws IOException {
        PlantUMLClassDiagramConfigBuilder
                configBuilder = new PlantUMLClassDiagramConfigBuilder(scanPackages)
                .withHideClasses(hideClasses);
        PlantUMLClassDiagramGenerator generator = new PlantUMLClassDiagramGenerator(configBuilder.build());
        String generatedDiagram = generator.generateDiagramText();
        writeToFile(filename + "_" + tag + "_ClassDiagram.puml", generatedDiagram);
    }

    private void writeToFile(final String fileName, String generatedDiagram) throws IOException {
        PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8);
        writer.println(generatedDiagram);
        writer.close();
    }
}

