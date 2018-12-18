package com.analyzer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Start {

//    private static String originalPath = "./samples/startbootstrap-sb-admin-2-examples/sample-0-origin.html";
//    private static String diffPath = "./samples/startbootstrap-sb-admin-2-examples/sample-1-evil-gemini.html";
//    private static String originalButtonId = "make-everything-ok-button";

    private static String CHARSET_NAME = "utf8";
    private static Map<String, String> attributes = new HashMap<>();
    private static String attributeFormat = "a[%s=%s]";

    public static void main (String[] args) {
        String originalPath;
        String diffPath;
        String originalButtonId;
        // 2 - at least parameter amount
        if(args.length > 2){
            originalPath = args[0];
            diffPath = args[1];
            originalButtonId = args[2];
        }else {
            System.out.println("Enter input parameters: original file path, diff-case file path and original element ID");
            return;
        }

        File diffFile = new File(diffPath);
        Element originalElement = findElementById(new File(originalPath), originalButtonId);

        //to use the app without console start, comment code above and uncomment code bellow
//        File diffFile = new File(resourcePathToDiffFile);
//        Element originalElement = findElementById(new File(resourcePathToOriginalFile), originalButtonId);

        if(Objects.isNull(originalElement)){
            System.out.println("Original element is not found!");
            return;
        }

        originalElement.attributes().asList()
                .stream()
                .filter(attr -> attr.getKey().equals("class")
                             || attr.getKey().equals("title")
                             || attr.getKey().equals("href"))
                .forEach(attr -> attributes.put(attr.getKey(), attr.getValue()));

        List<Element> diffElements = new ArrayList<>();
        for (Map.Entry entry: attributes.entrySet()) {
            String format = String.format(attributeFormat, entry.getKey(), entry.getValue());
            Elements diffCaseElement = findElementByAttribute(diffFile, format);
            if(Objects.nonNull(diffCaseElement)){
                addNewElements(diffElements, diffCaseElement);
            }
        }

        //the most similar element will be put here
        Element mostSimilarElement = null;
        int maxCount = 0;
        for (Element element: diffElements) {
            int count = 0;
            //check the text similarity
            if (element.text().equals(originalElement.text())) {
                count++;
            }
            //check the attributes similarity
            for(Map.Entry<String, String> attrs: attributes.entrySet()){
                String key = attrs.getKey();
                if(element.attr(key).equals(attrs.getValue())){
                    count++;
                }
            }
            if(maxCount<count){
               mostSimilarElement = element;
               maxCount = count;
            }
        }
        if(Objects.isNull(mostSimilarElement)){
            System.out.println("There is no similar element!");
            return;
        }
        showSimilarElementsHTMLPath(mostSimilarElement);
    }


    private static void showSimilarElementsHTMLPath(Element diffElement) {
            System.out.println("Element: <" + diffElement.nodeName() + "> with text: '" + diffElement.text() + "' has attributes:");
            String attributes = diffElement.attributes().asList().stream()
                    .map(attr -> attr.getKey() + " = " + attr.getValue())
                    .collect(Collectors.joining(", "));
            System.out.println(attributes);
            System.out.println("The element path in html: " + getHTMLNodePath(diffElement));
    }

    private static void addNewElements(List<Element> toElements, Elements fromElements) {
        outer: for (Element newElement: fromElements) {
            inner: for (Element existElement: toElements) {
                //compare all attributes values for new and exist elements
                for(Attribute attribute: newElement.attributes()){
                    String key = attribute.getKey();
                    //if at least one attr is different, take another element.
                    //if the current element was last in the list 'toElements'
                    //(it mean the current element is unique in the list) we add this one
                    if(!attribute.getValue().equals(existElement.attr(key))){
                        continue inner;
                    }
                }
                //if all attrs of elements is equal, we skip add step and take next element
                continue outer;
            }
            toElements.add(newElement);
        }
    }

    //return element html path
    private static String getHTMLNodePath(Element element) {
        Elements parents = element.parents();
        String path = element.nodeName() + " > " + parents.stream()
                .map(Element::nodeName).collect(Collectors.joining(" > "));
        return path;
    }

    private static Element findElementById(File htmlFile, String targetElementId) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return doc.getElementById(targetElementId);
        } catch (IOException e) {
            return null;
        }
    }

    private static Elements findElementByAttribute(File htmlFile, String elementAttributeValue) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return doc.select(elementAttributeValue);
        } catch (IOException e) {
            return null;
        }
    }

}
