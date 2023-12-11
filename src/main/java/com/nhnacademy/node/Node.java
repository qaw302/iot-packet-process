package com.nhnacademy.node;

import lombok.extern.slf4j.Slf4j;

/**
 * @field count 생성된 노드 개수
 * @field id 각 노드의 id
 */
@Slf4j
public abstract class Node {

    static int count;
    String id;

    Node() {
        count++;
        id = String.format("%s-%02d", getClass().getSimpleName(), count);
        log.trace("create node : {}", id);
    }

    public String getId() {
        return id;
    }

    public abstract String getName();

    public abstract void setName(String name);

    public static int getCount() {
        return count;
    }
}
