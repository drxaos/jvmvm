package com.googlecode.jvmvm.ui;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Code implements Serializable {

    public abstract static class Edit implements Serializable {
        int offset;

        abstract public void apply(Section to, int startOffset);

        public int getOffset() {
            return offset;
        }

        abstract public int getLength();
    }

    public static class Insert extends Edit {
        String string;

        public Insert(String string, int offset) {
            this.string = string;
            this.offset = offset;
        }

        @Override
        public void apply(Section to, int startOffset) {
            to.text = new StringBuilder(to.text).insert(offset - startOffset, string).toString();
        }

        @Override
        public int getLength() {
            return 0;
        }
    }

    public static class Remove extends Edit {
        int length;

        public Remove(int length, int offset) {
            this.length = length;
            this.offset = offset;
        }

        @Override
        public void apply(Section to, int startOffset) {
            to.text = to.text.substring(0, offset - startOffset) + to.text.substring(offset - startOffset + length, to.text.length());
        }

        @Override
        public int getLength() {
            return length;
        }
    }

    public static class Replace extends Edit {
        String string;
        int length;

        public Replace(String string, int offset, int length) {
            this.string = string;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void apply(Section to, int startOffset) {
            String edit = to.text.substring(0, offset - startOffset) + to.text.substring(offset - startOffset + length, to.text.length());
            edit = new StringBuilder(edit).insert(offset - startOffset, string).toString();
            to.text = edit;
        }

        @Override
        public int getLength() {
            return length;
        }
    }


    public static class Section implements Serializable {
        boolean editable;
        boolean startOfStartLevel;
        boolean endOfStartLevel;
        boolean leading;
        boolean trailing;
        boolean inline;
        String text;

        public Section(boolean editable, boolean startOfStartLevel, boolean endOfStartLevel, boolean leading, boolean trailing, boolean inline, String text) {
            this.editable = editable;
            this.startOfStartLevel = startOfStartLevel;
            this.endOfStartLevel = endOfStartLevel;
            this.leading = leading;
            this.trailing = trailing;
            this.inline = inline;
            this.text = text;
            if (text == null) {
                this.text = "";
            }
        }

        @Override
        public String toString() {
            return "Section{" +
                    "editable=" + editable +
                    ", startOfStartLevel=" + startOfStartLevel +
                    ", endOfStartLevel=" + endOfStartLevel +
                    ", leading=" + leading +
                    ", trailing=" + trailing +
                    ", inline=" + inline +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    List<Section> sections = new ArrayList<Section>();

    public boolean apply(Edit codeEdit) {
        Section editSection = null;
        int startOffset = 0;
        for (Section section : sections) {
            if (codeEdit.getOffset() >= startOffset && codeEdit.getOffset() + codeEdit.getLength() < startOffset + section.text.length()) {
                editSection = section;
                break;
            }
            startOffset += section.text.length();
        }
        if (editSection == null) {
            return false;
        }
        if (!editSection.editable) {
            return false;
        }
        codeEdit.apply(editSection, startOffset);
        return true;
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

    public static class Line {
        public boolean readonly;
        public boolean inline = false;
        public int inlineStart, inlineEnd;

        public Line(boolean readonly) {
            this.readonly = readonly;
        }

        public Line(boolean readonly, boolean inline, int inlineStart, int inlineEnd) {
            this.readonly = readonly;
            this.inline = inline;
            this.inlineStart = inlineStart;
            this.inlineEnd = inlineEnd;
        }
    }

    public HashMap<Integer, Line> getReadonlyLines() {
        HashMap<Integer, Line> res = new HashMap<Integer, Line>();
        int beforeInline = 0;
        int line = 0;
        for (Section section : sections) {
            for (int i = 0; i < StringUtils.countMatches(section.text, "\n"); i++) {
                if (!section.editable && !res.containsKey(line)) {
                    res.put(line, new Line(true));
                }
                line++;
            }
            if (!section.editable && section.inline) {
                beforeInline = section.text.length();
            }
            if (section.editable && section.inline) {
                // inline
                res.put(line, new Line(true));
                res.get(line).inline = true;
                res.get(line).inlineStart = beforeInline - 1; // -1 because first quote excluded and last included
                res.get(line).inlineEnd = beforeInline + section.text.length();
            }
        }
        if (!res.containsKey(line)) {
            res.put(line, new Line(true));
        }
        return res;
    }

    public static Code parse(String template) {
        Code code = new Code();
        StringBuilder b = new StringBuilder();

        boolean editable = false;
        boolean leading = true;

        for (String s : template.replace("\r\n", "\n").replace("\r", "\n").split("\n")) {
            if (s.equals("/*BEGIN_EDITABLE*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, false, b.toString()));
                b = new StringBuilder();
                leading = false;
                editable = true;
            } else if (s.equals("/*END_EDITABLE*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, false, b.toString()));
                b = new StringBuilder();
                leading = false;
                editable = false;
            } else if (s.contains("/*BEGIN_INLINE*/") && s.contains("/*END_INLINE*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, false, b.toString()));

                b = new StringBuilder();
                b.append(s.substring(0, s.indexOf("/*BEGIN_INLINE*/")));
                code.sections.add(new Section(false, false, false, leading, false, true, b.toString()));
                b = new StringBuilder();
                b.append(s.substring(s.indexOf("/*BEGIN_INLINE*/") + "/*BEGIN_INLINE*/".length(), s.indexOf("/*END_INLINE*/")));
                code.sections.add(new Section(true, false, false, leading, false, true, b.toString()));
                b = new StringBuilder();
                b.append(s.substring(s.indexOf("/*END_INLINE*/") + "/*END_INLINE*/".length(), s.length()));
                code.sections.add(new Section(false, false, false, leading, false, true, b.toString()));

                b = new StringBuilder();
                leading = false;
            } else if (s.equals("/*START_OF_START_LEVEL*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, false, b.toString()));
                b = new StringBuilder();
                leading = false;
                code.sections.add(new Section(false, true, false, false, false, false, null));
            } else if (s.equals("/*END_OF_START_LEVEL*/")) {
                code.sections.add(new Section(editable, false, false, leading, false, false, b.toString()));
                b = new StringBuilder();
                leading = false;
                code.sections.add(new Section(false, false, true, false, false, false, null));
            } else {
                b.append(s);
                b.append("\n");
            }
        }
        code.sections.add(new Section(editable, false, false, leading, true, false, b.toString() + "\n"));

        return code;
    }
}
