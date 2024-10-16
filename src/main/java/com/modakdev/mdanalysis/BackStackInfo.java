package com.modakdev.mdanalysis;

import javafx.scene.Scene;

import java.util.Stack;

public class BackStackInfo {
    Stack<Scene> sceneStack;
    Stack<String> titleStack;

    public BackStackInfo(Stack<Scene> sceneStack, Stack<String> titleStack) {
        this.sceneStack = sceneStack;
        this.titleStack = titleStack;
    }

    public BackStackInfo() {
        sceneStack = new Stack<>();
        titleStack = new Stack<>();
    }

    public Stack<Scene> getSceneStack() {
        return sceneStack;
    }

    public void setSceneStack(Stack<Scene> sceneStack) {
        this.sceneStack = sceneStack;
    }

    public Stack<String> getTitleStack() {
        return titleStack;
    }

    public void setTitleStack(Stack<String> titleStack) {
        this.titleStack = titleStack;
    }
}
