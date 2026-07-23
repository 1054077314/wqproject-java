package com.campus.common;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PageUtils {

    private PageUtils() {
    }

    public static <T> PageResult<T> paginate(HttpServletRequest request, int defaultPageSize, Supplier<List<T>> query) {
        return paginateMapped(request, defaultPageSize, query, Function.identity());
    }

    public static <S, T> PageResult<T> paginateMapped(HttpServletRequest request, int defaultPageSize,
                                                       Supplier<List<S>> query, Function<List<S>, List<T>> mapper) {
        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = parseInt(request.getParameter("page_size"), defaultPageSize);
        PageHelper.startPage(page, pageSize);
        List<S> rows = query.get();
        PageInfo<S> info = new PageInfo<>(rows);
        List<T> list = mapper.apply(rows);
        String base = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        String next = null;
        String previous = null;
        if (info.isHasNextPage()) {
            next = buildPageUrl(base, queryString, info.getNextPage(), pageSize);
        }
        if (info.isHasPreviousPage()) {
            previous = buildPageUrl(base, queryString, info.getPrePage(), pageSize);
        }
        return new PageResult<>(info.getTotal(), next, previous, list);
    }

    private static String buildPageUrl(String base, String queryString, int page, int pageSize) {
        StringBuilder sb = new StringBuilder(base).append("?page=").append(page).append("&page_size=").append(pageSize);
        if (queryString != null && !queryString.isBlank()) {
            for (String part : queryString.split("&")) {
                if (part.startsWith("page=") || part.startsWith("page_size=")) {
                    continue;
                }
                sb.append("&").append(part);
            }
        }
        return sb.toString();
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
