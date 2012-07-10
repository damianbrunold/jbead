/** jbead - http://www.brunoldsoftware.ch
    Copyright (C) 2001-2012  Damian Brunold

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ch.jbead.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.jbead.Version;

public class VersionBumper {

    public static void bump(Version oldversion, Version newversion) {
        System.out.println("bump version from " + oldversion.getVersionString() + " to " + newversion.getVersionString());
        try {
            writeVersionFile(newversion);
            patchLaunch4jConfig(newversion);
            patchSetupConfig(newversion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeVersionFile(Version version) throws IOException {
        Writer writer = new FileWriter("src/version.txt");
        try {
            writer.write(version.getVersionString() + "\n");
        } finally {
            writer.close();
        }
    }

    private static void patchLaunch4jConfig(Version version ) throws Exception {
        Document doc = readConfigFile();
        patchConfig(doc, version);
        writeConfigFile(doc);
    }

    private static Document readConfigFile() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(getLaunchConfigFile());
    }

    private static void patchConfig(Document doc, Version version) {
        patchVersions(doc, "File", version);
        patchVersions(doc, "Product", version);
    }

    private static void patchVersions(Document doc, String type, Version version) {
        patchTechVersion(doc, type.toLowerCase() + "Version", version);
        patchTxtVersion(doc, "txt" + type + "Version", version);
    }

    private static void patchTechVersion(Document doc, String name, Version version) {
        patchVersion(doc, name, version.getWinVersionString());
    }

    private static void patchTxtVersion(Document doc, String name, Version version) {
        patchVersion(doc, name, version.getShortVersionString());
    }

    private static void patchVersion(Document doc, String name, String version) {
        Element element = (Element) doc.getElementsByTagName(name).item(0);
        element.getChildNodes().item(0).setNodeValue(version);
    }

    private static void writeConfigFile(Document doc) throws Exception {
        Transformer serializer= SAXTransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(getLaunchConfigFile());
        serializer.transform(source, result);
    }

    private static File getLaunchConfigFile() {
        return new File("starter_win/jbead_launch4j.xml");
    }

    private static void patchSetupConfig(Version version) throws IOException {
        List<String> lines = readSetupConfig();
        patchSetupConfig(lines, version);
        writeSetupConfig(lines);
    }

    private static List<String> readSetupConfig() throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(getSetupConfigFile()));
        try {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return lines;
    }

    private static void patchSetupConfig(List<String> lines, Version version) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("OutFile ")) {
                lines.set(i, "OutFile \"jbead_" + version.getVersionString() + "_setup.exe\"");
            }
        }
    }

    private static void writeSetupConfig(List<String> lines) throws IOException {
        Writer writer = new FileWriter(getSetupConfigFile());
        try {
            for (String line : lines) {
                writer.write(line + "\r\n");
            }
        } finally {
            writer.close();
        }
    }

    private static File getSetupConfigFile() {
        return new File("setup_win/jbead.nsi");
    }

}
