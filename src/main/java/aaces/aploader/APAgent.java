package aaces.aploader;

import aaces.aploader.transformers.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class APAgent {

    private static PrintWriter log;

    private static final ClassFileTransformer[] transformers = {
            new GameManagerTransformer(),
            new MenuPanelGUITransformer(),
            new GameMenuGuiTransformer(),
            new GameMenuBackgroundTransformer(),
            new ClaimButtonGuiTransformer(),
            new TaskInfoFrameUiTransformer(),
            new MyKeyboardTransformer(),
            new UnlockGuideUiTransformer(),
            new SessionTransformer(),
            new SessionManagerTransformer(),
            new SavesTransformer()
    };

    public static void premain(
            String args,
            Instrumentation instrumentation) {

        try {
            log = new PrintWriter(
                    new FileWriter("APEqLog.log", false),
                    true
            );

            log.println("Agent loaded! Mod version " + APBridge.getModVersion());
            log.println("Java version: "
                    + System.getProperty("java.version"));
            log.println("Java home: "
                    + System.getProperty("java.home"));

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for(ClassFileTransformer transformer : transformers){
            instrumentation.addTransformer(transformer);
        }

        log.println("Transformers registered.");
    }

    public static void log(String message) {
        log.println("[AP] " + message);
        System.out.println("[AP] " + message);
    }
}