package com.ussveritas.artemis.proxy.observer;
import com.walkertribe.ian.enums.Console;
public enum GhostType{ENGINEERING(Console.ENGINEERING),WEAPONS(Console.WEAPONS),COMMUNICATIONS(Console.COMMUNICATIONS);private final Console console;GhostType(Console c){this.console=c;}public Console getConsole(){return console;}}