package tk.zielony.handylib;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.util.TypedValue;

import java.lang.reflect.Field;
import java.util.List;

public class StringUtils {
    private StringUtils() {
    }

    public static void replaceStrings(@NonNull List<StringRes> strings) {
        Context context = Handylib.getApplication();

        try {
            AssetManager assets = context.getAssets();
            Field stringBlocksField = assets.getClass().getDeclaredField("mStringBlocks");
            stringBlocksField.setAccessible(true);
            Object[] stringBlocks = (Object[]) stringBlocksField.get(assets);

            Object stringBlock = stringBlocks[2];
            Field mSparseStringsField = stringBlock.getClass().getDeclaredField("mSparseStrings");
            mSparseStringsField.setAccessible(true);
            SparseArray<String> mSparseStrings = (SparseArray<String>) mSparseStringsField.get(stringBlock);

            TypedValue value = new TypedValue();
            for (StringRes s : strings) {
                context.getResources().getValue(s.resId, value, true);
                mSparseStrings.put(value.data, s.value);
            }
        } catch (NoSuchFieldException e) {
            Handylib.logReflectionError(e);
        } catch (IllegalAccessException e) {
            Handylib.logReflectionError(e);
        }
    }
}
