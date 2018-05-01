package com.deepinspire.gmatclub.storage;

import android.content.Context;

/**
 * Created by root on 26.03.18.
 */

public class Injection {
    public static Repository getRepository(Context ctx) {
        return Repository.getInstance(ctx);
    }
}
