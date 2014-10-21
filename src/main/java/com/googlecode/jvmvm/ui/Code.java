package com.googlecode.jvmvm.ui;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Code {

    public abstract static class Edit implements Serializable {
        abstract public String apply(String to);
    }

    public static class Insert extends Edit {
        String string;
        int offset;

        public Insert(String string, int offset) {
            this.string = string;
            this.offset = offset;
        }

        public String apply(String to) {
            return new StringBuilder(to).insert(offset, string).toString();
        }
    }

    public static class Remove extends Edit {
        int offset, length;

        public Remove(int length, int offset) {
            this.length = length;
            this.offset = offset;
        }

        public String apply(String to) {
            return to.substring(0, offset) + to.substring(offset + length, to.length());
        }
    }

    public static class Replace extends Edit {
        String string;
        int offset, length;

        public Replace(String string, int offset, int length) {
            this.string = string;
            this.offset = offset;
            this.length = length;
        }

        public String apply(String to) {
            String edit = to.substring(0, offset) + to.substring(offset + length, to.length());
            edit = new StringBuilder(edit).insert(offset, string).toString();
            return edit;
        }
    }


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

        @Override
        public String toString() {
            return "Section{" +
                    "editable=" + editable +
                    ", startOfStartLevel=" + startOfStartLevel +
                    ", endOfStartLevel=" + endOfStartLevel +
                    ", leading=" + leading +
                    ", trailing=" + trailing +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    List<Section> sections = new ArrayList<Section>();

    public boolean apply(List<? extends Edit> codeEdits) {
        for (Edit codeEdit : codeEdits) {

            // TODO try apply edits

        }
        return false;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Section section : sections) {
            if (section.text != null) {
                b.append(section.text);
            }
        }
        return b.toString();
    }

    public String toCompilationUnit(String secret) {
        StringBuilder b = new StringBuilder();
        for (Section section : sections) {
            if (section.text != null) {
                b.append(section.text);
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

    public List<Integer> getReadonlyLines() {
        List<Integer> res = new ArrayList<Integer>();
        int line = 0;
        for (Section section : sections) {
            for (int i = 0; i < StringUtils.countMatches(section.text, "\n"); i++) {
                if (!section.editable) {
                    res.add(line);
                }
                line++;
            }
        }
        res.add(line);
        return res;
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
                b.append(s);
                b.append("\n");
            }
        }
        code.sections.add(new Section(editable, false, false, leading, true, b.toString() + "\n"));

        return code;
    }
}
