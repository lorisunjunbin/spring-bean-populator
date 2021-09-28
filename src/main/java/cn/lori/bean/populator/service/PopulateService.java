package cn.lori.bean.populator.service;

import cn.lori.bean.populator.model.PopulatorResource;
import cn.lori.bean.populator.model.Result;
import io.vavr.control.Try;

import java.util.List;

/**
 * {@link cn.lori.bean.populator.annotation.PopulatorCategory }
 * {@link cn.lori.bean.populator.annotation.PopulatorItem }
 */
public interface PopulateService {

    Integer syncIntervalInMinutes();

    /**
     * reload resource(s) and populate immediately。
     *
     * @param action, 'all' OR bean identity of spring container.
     */
    <R> Result<R> manuallyReload(String action);

    <T> Try<T> fetch(Class<T> clazz);

    <T> Try<T> populate(T instance);

    <T> Try<T> refresh(T instance);

    <T> Try<T> refreshClass(Class<T> clazz);

    Try<Void> loadAll();

    Try<Void> refreshAll();

    /**
     * Load resources associated with given category.
     *
     * @param categoryCode
     * @return List of resource associated with given category.
     */
    List<PopulatorResource> loadItems(String categoryCode);

}