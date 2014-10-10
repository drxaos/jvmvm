package com.googlecode.jvmvm.ui.levels.level_01.internal;

import java.util.ArrayList;
import java.util.List;

public class Code {

    public static class Section {
        boolean editable;
        boolean startOfStartLevel;
        boolean endOfStartLevel;
        boolean leading;
        boolean trailing;
        String text;

        public Section(boolean editable, boolean startOfStartLevel, boolean endOfStartLevel, boolean leading, boolean trailing, String text) {
            this.editable = editable;
            this.startOfStartLevel = startOfStartLevel;
            this.endOfStartLevel = endOfStartLevel;
            this.leading = leading;
            this.trailing = trailing;
            this.text = text;
        }

    }

    List<Section> sections = new ArrayList<Section>();


    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Section section : sections) {
            if (section.text != null) {
                b.append(section.text).append("\n");
            }
        }
        return b.toString();
    }

    public String toCompilationUnit(String secret) {
        StringBuilder b = new StringBuilder();
        for (Section section : sections) {
            if (section.text != null) {
                b.append(section.text).append("\n");
            }
            if (section.startOfStartLevel) {
                b.append("map.__auth(\"startOfStartLevel\",\"").append(secret).append("\");").append("\n");
            }
            if (section.endOfStartLevel) {
                b.append("map.__auth(\"endOfStartLevel\",\"").append(secret).append("\");").append("\n");
            }
        }
        return b.toString();
    }

    public boolean apply(String code, boolean verify) {
        if (!verify && !apply(code, true)) {
            return false;
        }

        code = code.replace("\r\n", "\n").replace("\r", "\n");
        Section waitingEditable = null;
        for (Section section : sections) {
            if (!section.editable && section.leading) {
                if (!code.startsWith(section.text)) {
                    return false;
                }
                code = code.substring(section.text.length());
            }

            if (!section.editable && !section.leading) {
                int found = code.indexOf(section.text);
                if (found <= 0) {
                    return false;
                }
                if (!verify) {
                    waitingEditable.text = code.substring(0, found);
                }
                code = code.substring(found);
                code = code.substring(section.text.length());
            }

            if (!section.editable && section.trailing) {
                int found = code.indexOf(section.text);
                if (found <= 0) {
                    return false;
                }
                if (!verify) {
                    waitingEditable.text = code.substring(0, found);
                }
                code = code.substring(found);
                code = code.substring(section.text.length());
                if (!code.isEmpty()) {
                    return false;
                }
            }

            if (!verify) {
                if (section.editable && section.leading) {
                    waitingEditable = section;
                }

                if (section.editable && !section.leading) {
                    waitingEditable = section;
                }

                if (section.editable && section.trailing) {
                    section.text = code;
                }
            }
        }
        return true;
    }


    public static Code parse(String template) {
        Code code = new Code();
        StringBuilder b = new StringBuilder();

        boolean editable = false;
        boolean leading = true;

        for (String s : template.replace("\r\n", "\n").replace("\r", "\n").split("\n")) {
            if (s.equals("/*BEGIN_EDITABLE*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, b.toString()));
                b = new StringBuilder();
                leading = false;
                editable = true;
            } else if (s.equals("/*END_EDITABLE*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, b.toString()));
                b = new StringBuilder();
                leading = false;
                editable = false;
            } else if (s.equals("/*START_OF_START_LEVEL*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, b.toString()));
                b = new StringBuilder();
                leading = false;
                code.sections.add(new Section(false, true, false, false, false, null));
            } else if (s.equals("/*END_OF_START_LEVEL*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, b.toString()));
                b = new StringBuilder();
                leading = false;
                code.sections.add(new Section(false, false, true, false, false, null));
            } else {
                b.append(s).append("\n");
            }
        }
        code.sections.add(new Section(editable, false, false, leading, true, b.toString()));

        return code;
    }
}
