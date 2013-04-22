package com.rodhilton.rectanglevisibility.gui

import com.rodhilton.rectanglevisibility.main.AppState
import com.rodhilton.rectanglevisibility.main.AppStateListener
import org.apache.commons.lang.StringUtils

class TerminalInterface {
    private static int NAME_LENGTH=15
    private static String lastDisplay=""
    private static char[] SPINNER=['|','-','\\','|','/','-','\\']

    public static void launchInterface(AppState appState, PrintStream out = System.out, boolean newLines) {

        appState.register(new AppStateListener() {
            @Override
            void updateState(AppState state) {
                if(state.completed) {
                    out.println("\nCompleted!")
                    out.println(state.diagram.toString())
                    System.exit(0)
                } else {
                    String nameAbbrev = StringUtils.abbreviate(state.name, NAME_LENGTH)
                    String nameDisplay = StringUtils.leftPad(nameAbbrev,NAME_LENGTH)

                    def toDisplay = sprintf("[${nameDisplay}] K${state.maxRect}: Gen#: %-8d Score: %d/%d  %s  ", state.currentGeneration(), state.diagram.fitness(), state.diagram.goal, SPINNER[(state.currentGeneration()-1) % SPINNER.length])

                    if(newLines) {
                        out.println(toDisplay)
                    } else {
                        out.print("\r"+toDisplay)
                    }
                }
            }
        })
    }
}
