package com.tickrobot.ide.nativeticide;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.GetCodeCallback;
import com.google.blockly.android.codegen.CodeGenerationRequest;
import com.google.blockly.android.codegen.LoggingCodeGeneratorCallback;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AbstractBlocklyActivity {
    private static final String TAG = "SimpleActivity";

    private static final List<String> BLOCK_DEFINITIONS = Arrays.asList(
            "mblocks/pins.json",
            "mblocks/inout_blocks.json",
            "mblocks/control_blocks.json",
            "mblocks/math_blocks.json",
            "mblocks/text_blocks.json",
            "mblocks/logic_blocks.json",
            "mblocks/serial_blocks.json"

//            "googleblocks/logic_blocks.json"
//            "googleblocks/math_blocks.json",
//            "googleblocks/text_blocks.json",
//            "googleblocks/list_blocks.json",
//            "googleblocks/colour_blocks.json",
//            "googleblocks/variable_blocks.json"

    );
    private static final List<String> JAVASCRIPT_GENERATORS = Arrays.asList(
            // Custom block generators go here. Default blocks are already included.
            // TODO(#99): Include Javascript defaults when other languages are supported.
            "arduino/digitalPinToInterrupt.js",
            "arduino.js",
//            "javascript_compressed.js",
            "arduino/pins.js",
            "arduino/inout.js",
            "arduino/control.js",
            "arduino/math.js",
            "arduino/text.js",
            "arduino/lists.js",
            "arduino/logic.js",
            "arduino/blockgroup.js",
            "arduino/storage.js",

            "arduino/ethernet.js",

            "arduino/factory.js",
            "arduino/procedures.js",
            "arduino/sensor.js",
            "arduino/variables.js"

    );

    CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new LoggingCodeGeneratorCallback(this, TAG);

    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        return BLOCK_DEFINITIONS;
    }

    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        return "mblocks/toolbox.xml";
    }

    @NonNull
    @Override
    protected List<String> getGeneratorsJsPaths() {
        return JAVASCRIPT_GENERATORS;
    }

    @NonNull
    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        // Uses the same callback for every generation call.

        return mCodeGeneratorCallback;
    }

    @Override
    protected void onInitBlankWorkspace() {
        // Initialize variable names.
        // TODO: (#22) Remove this override when variables are supported properly
        getController().addVariable("item");
        getController().addVariable("leo");
        getController().addVariable("don");
        getController().addVariable("mike");
        getController().addVariable("raf");
    }



}
