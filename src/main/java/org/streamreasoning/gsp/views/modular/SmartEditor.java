package org.streamreasoning.gsp.views.modular;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasLabel;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;

public class SmartEditor extends AceEditor {
    public SmartEditor(String queryTemplate) {
        setValue(queryTemplate);
        this.setMode(AceMode.sql);
    }


    public void setUpAce(AceEditor ace) {

        //
//        ArrayList<String> custom = new ArrayList<String>();
//        custom.add("REGISTER");
//        custom.add("QUERY");
//        custom.add("MATCH");
//        custom.add("WHERE");
//        custom.add("STARTING");
//        custom.add("WITH");
//        custom.add("WITHIN");
//        custom.add("AT");
//        custom.add("EMIT");
//        custom.add("SNAPSHOT");
//        custom.add("ON");
//        custom.add("ENTERING");
//        custom.add("EVERY");
//        custom.add("EMIT");
        //
//        AceCustomMode customMode = new AceCustomMode();

//        AceCustomModeRule keywords = new AceCustomModeRule();
//        keywords.setRegex("[a-zA-Z_$][a-zA-Z0-9_$]*\\b");
//        keywords.setKeywordMapper(
//                Map.of(
//                        AceCustomModeTokens.KEYWORD, String.join("|", custom)
//                ),
//                AceCustomModeTokens.IDENTIFIER,
//                true,
//                "|"
//        );


//        ArrayList<String> fs = new ArrayList<String>();
//        fs.add("allShortestPaths");

//        AceCustomModeRule functions = new AceCustomModeRule();
//        functions.setRegex("[a-z][a-zA-Z0-9]*\\b");
//        functions.setKeywordMapper(
//                Map.of(AceCustomModeTokens.VARIABLE, String.join("|", fs)),
//                AceCustomModeTokens.VARIABLE,
//                true,
//                "|"
//        );


//        AceCustomModeRule lineComment = new AceCustomModeRule();
//        lineComment.setRegex("--.*$");
//        lineComment.setToken(AceCustomModeTokens.COMMENT);
        //
//        AceCustomModeRule blockComment = new AceCustomModeRule();
//        blockComment.setStart("/\\*");
//        blockComment.setEnd("\\*/");
//        blockComment.setToken(AceCustomModeTokens.COMMENT);
        //
//        customMode.addState(
//                "start",
//                lineComment,
//                blockComment,
//                functions,
//                keywords
//        );

//        ace.addCustomMode("cypher", customMode);
//        ace.setCustomMode("cypher");
        ace.setMode(AceMode.sql);
    }
}
