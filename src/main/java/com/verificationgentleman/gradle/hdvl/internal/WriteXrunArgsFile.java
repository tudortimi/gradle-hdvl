package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.*;
import org.gradle.api.provider.Provider;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class WriteXrunArgsFile implements TransformAction<TransformParameters.None> {
    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        File input = getInputArtifact().get().getAsFile();
        File xrunArgsFile = outputs.file(input.getName() + ".xrun_args.f");
        File[] svSourceFiles = getSvSourceFiles(input);
        writeXrunArgsFile(input, xrunArgsFile, svSourceFiles);
    }

    private static File[] getSvSourceFiles(File input) {
        File compileSpec = new File(input, ".gradle-hdvl/compile-spec.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(compileSpec);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr = xpath.compile("/compileSpec/svSourceFiles/svSourceFile");
            NodeList svSourceFiles = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            assert svSourceFiles != null;

            File[] result = new File[svSourceFiles.getLength()];
            for (int i = 0; i < svSourceFiles.getLength(); i++) {
                result[i] = new File(input, svSourceFiles.item(i).getTextContent());
                assert result[i].isAbsolute() : "not absolute: " + result[i];
                assert result[i].exists() : "doesn't exist: " + result[i];
            }
            return result;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeXrunArgsFile(File input, File xrunArgsFile, File[] svSourceFiles) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xrunArgsFile, true))) {
            writer.write("-makelib worklib\n");
            for (File svSourceFile : svSourceFiles)
                writer.write("  " + svSourceFile + "\n");
            writer.write("-endlib\n");
        }
        catch (IOException ex) {
            ex.printStackTrace();  // TODO Implement better exception handling
        }
    }
}
