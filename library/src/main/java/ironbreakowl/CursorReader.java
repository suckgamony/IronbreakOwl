package ironbreakowl;

import android.database.Cursor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

class CursorReader {
    private static final HashMap<Class, CursorReader> sReaders = new HashMap<>();

    private static class MethodInfo {
        public Column column;
        public Class returnType;
    }

    public final HashMap<Method, MethodInfo> methods = new HashMap<>();

    public static <T> T create(final Cursor cursor, Class<T> clazz) {
        final CursorReader cr = getReader(clazz);
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                MethodInfo methodInfo = cr.methods.get(method);
                String columnName = methodInfo.column.value();
                Class returnType = methodInfo.returnType;
                int columnIndex = cursor.getColumnIndex(columnName);
                if (returnType == Integer.TYPE || returnType == Integer.class) {
                    return cursor.getInt(columnIndex);
                } else if (returnType == String.class) {
                    return cursor.getString(columnIndex);
                } else if (returnType == Long.TYPE || returnType == Long.class) {
                    return cursor.getLong(columnIndex);
                } else if (returnType == byte[].class) {
                    return cursor.getBlob(columnIndex);
                } else if (returnType == Float.TYPE || returnType == Float.class) {
                    return cursor.getFloat(columnIndex);
                } else if (returnType == Double.TYPE || returnType == Double.class) {
                    return cursor.getDouble(columnIndex);
                } else if (returnType == Short.TYPE || returnType == Short.class) {
                    return cursor.getShort(columnIndex);
                } else {
                    throw new IllegalArgumentException("Unsupported type: " + returnType.getCanonicalName());
                }
            }
        });
    }

    private static CursorReader getReader(Class clazz) {
        synchronized (sReaders) {
            CursorReader reader = sReaders.get(clazz);
            if (reader == null) {
                reader = parseClass(clazz);
                sReaders.put(clazz, reader);
            }
            return reader;
        }
    }

    private static CursorReader parseClass(Class clazz) {
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("Only interface is allowed: " + clazz.getCanonicalName());
        }

        CursorReader proxy = new CursorReader();
        HashMap<Method, MethodInfo> methods = proxy.methods;
        for (Method method : clazz.getMethods()) {
            Column column = method.getAnnotation(Column.class);
            if (column == null) continue;

            MethodInfo methodInfo = new MethodInfo();
            methodInfo.column = column;
            methodInfo.returnType = method.getReturnType();
            methods.put(method, methodInfo);
        }
        return proxy;
    }
}
