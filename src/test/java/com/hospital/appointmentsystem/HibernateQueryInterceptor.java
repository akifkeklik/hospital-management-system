package com.hospital.appointmentsystem;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HibernateQueryInterceptor implements StatementInspector {

    private static final ThreadLocal<List<String>> queries = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public String inspect(String sql) {
        queries.get().add(sql);
        return sql;
    }

    public static void startQueryCount() {
        queries.get().clear();
    }

    public static int getQueryCount() {
        return queries.get().size();
    }
    
    public static List<String> getQueries() {
        return new ArrayList<>(queries.get());
    }

    public static void clear() {
        queries.get().clear();
    }
}
