package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class WriteCompileSpecFile extends DefaultTask {
    private final RegularFileProperty destination;

    private ConfigurableFileCollection svSourceFiles;

    public WriteCompileSpecFile() {
        destination = getProject().getObjects().fileProperty();
        svSourceFiles = getProject().getObjects().fileCollection();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getSvSource() {
        return svSourceFiles;
    }

    @TaskAction
    protected void generate() {
        Document document = getDocument();
        writeFile(document);
    }

    private Document getDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element rootElement = document.createElement("compileSpec");
            document.appendChild(rootElement);

            Element svSourceFilesElement = document.createElement("svSourceFiles");
            rootElement.appendChild(svSourceFilesElement);

            for (File svSource : getSvSource().getFiles()) {
                Element svSourceFileElement = document.createElement("svSourceFile");
                svSourceFileElement.appendChild(document.createTextNode(getProject().relativePath(svSource)));
                svSourceFilesElement.appendChild(svSourceFileElement);
            }

            return document;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(destination.get().getAsFile());
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
