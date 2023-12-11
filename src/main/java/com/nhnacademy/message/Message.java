package com.nhnacademy.message;

public abstract class Message {

    static int count; // 메세지 개수
    final String id; // 메시지 id
    long creationTime; // 메세지 생성 시간

    Message() {
        // 메세지 생성시, count 증가, id 부여, 생성시간 부여
        count++;
        id = getClass().getSimpleName() + count;
        creationTime = System.currentTimeMillis();
    }

    // 필드의 getter 메서드
    public String getId() {
        return id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public static int getCount() {
        return count;
    }
}