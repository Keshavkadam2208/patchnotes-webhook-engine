package com.patchnotes.delivery_worker_service.entity;

public enum DeliveryStatus {
    SUCCESS,      //2xx response mila, kaam khtm
    RETRYING,     //Fail hua, par abhi retry queue me bheja gaya nahi hai
    EXHAUSTED_DLQ  //Sare retries khatm, ab dead letter queueme hai
}
