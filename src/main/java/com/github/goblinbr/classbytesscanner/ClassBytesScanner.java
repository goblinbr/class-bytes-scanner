package com.github.goblinbr.classbytesscanner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassBytesScanner {

    private Logger logger = Logger.getLogger(ClassBytesScanner.class.getName());

    /**
     * Scan class using UTF-8 for references of another classes, without recursion
     * @param clazz Class to scan
     * @return A Set with the classes that <b>clazz</b> references, or a empty Set if it doesn't reference any other class
     * @throws IOException if some I/O error occurs
     */
    public Set<Class> scanClass(Class clazz) throws IOException {
        return scanClass(clazz, "UTF-8", false, null);
    }

    /**
     * Scan class for references of another classes
     * @param clazz Class to scan
     * @param charsetName Witch charset that clazz is encoded
     * @param recursive If should scan recursively
     * @param ignoreIf Classes to ignore
     * @return A Set with the classes that <b>clazz</b> references, or a empty Set if it doesn't reference any other class
     * @throws IOException if some I/O error occurs
     */
    public Set<Class> scanClass(Class clazz, String charsetName, boolean recursive, IgnoreClassIf ignoreIf) throws IOException {
        Set<Class> classSet = new HashSet<Class>();
        scanClassRecursively( classSet, clazz, charsetName, recursive, ignoreIf );
        return classSet;
    }

    private void scanClassRecursively(Set<Class> classSet, Class clazz, String charsetName, boolean recursive, IgnoreClassIf ignoreIf) throws IOException {
        if (clazz != null) {
            if (charsetName == null) {
                charsetName = "UTF-8";
            }

            InputStream is = clazz.getResourceAsStream(clazz.getSimpleName() + ".class");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[102400];
            int bytesRead;
            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            data = buffer.toByteArray();
            buffer.close();
            is.close();
            Set<Class> classes = scanClassBytes(clazz, data, charsetName, ignoreIf);
            if (recursive) {
                for (Class c : classes) {
                    if (classSet.add(c)) {
                        scanClassRecursively(classSet, c, charsetName, recursive, ignoreIf);
                    }
                }
            } else {
                classSet.addAll(classes);
            }
        }
    }

    private Set<Class> scanClassBytes(Class clazz, byte[] clazzBytes, String charsetName, IgnoreClassIf ignoreIf) throws UnsupportedEncodingException {
        String classBytesString = new String(clazzBytes, charsetName);

        TreeSet<String> classNames = new TreeSet<String>();
        Pattern pattern = Pattern.compile("(([a-z]\\/([a-zA-Z0-9]{0,}\\/{0,})+)|([a-z][^L\\W]([a-zA-Z0-9]{0,}\\/)+))[a-zA-Z0-9]{1,}");
        Matcher matcher = pattern.matcher(classBytesString);
        while(matcher.find()) {
            String possibleClassName = classBytesString.substring(matcher.start(), matcher.end());
            String[] nameSplit = possibleClassName.split("/");
            String simpleClassName = nameSplit[nameSplit.length-1];
            if(Character.isUpperCase(simpleClassName.charAt(0))){
                classNames.add(possibleClassName.replace("/", "."));
            }
        }

        HashSet<Class> classSet = new HashSet<Class>();
        for(String possibleClassName : classNames){
            try{
                Class c = Class.forName(possibleClassName);
                if (!c.getName().equals(clazz.getName()) && (ignoreIf == null || !ignoreIf.ignoreIf(c))) {
                    classSet.add(c);
                }
            }
            catch (NoClassDefFoundError error){
                logger.info("NoClassDefFoundError " + possibleClassName);
            }
            catch (ClassNotFoundException ex){
                logger.info("ClassNotFoundException " + possibleClassName);
            }
        }

        return classSet;
    }

}
