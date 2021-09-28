package cn.lori.bean.populator.service.impl;

import cn.lori.bean.populator.annotation.PopulatorCategory;
import cn.lori.bean.populator.annotation.PopulatorItem;
import cn.lori.bean.populator.model.PopulatorResource;
import cn.lori.bean.populator.model.Result;
import cn.lori.bean.populator.service.PopulateService;
import cn.lori.bean.populator.util.JacksonUtils;
import cn.lori.bean.populator.util.ReflectionUtils;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Component
public abstract class BasePopulateService implements PopulateService {

    private final static Logger logger = LogManager.getLogger(BasePopulateService.class);

    private final LoadingCache<String, List<PopulatorResource>> cache = initLoadingCache();

    private Map<String, Object> beanName2InstanceWhichDesire2Sync = new ConcurrentHashMap<>();

    private ScheduledExecutorService executor;

    @Autowired
    ApplicationContext ctx;

    @EventListener(ApplicationReadyEvent.class)
    @Override
    public Try<Void> loadAll() {
        return Try.of(() -> {

            registerSyncIfAvailable();

            ctx.getBeansWithAnnotation(PopulatorCategory.class).entrySet().stream()
                    .forEach(entry -> populate(entry.getValue())
                            .onSuccess(config -> logger.debug(entry.getKey() + " Loaded successfully."))
                            .onFailure(logger::error));
            return (null);
        });
    }

    @Override
    public Integer syncIntervalInMinutes() {
        return 5;//default 5 minutes.
    }

    private void registerSyncIfAvailable() {
        ctx.getBeansWithAnnotation(PopulatorCategory.class).entrySet().stream()
                .forEach(entry -> {
                    String beanIdentity = entry.getKey();
                    Object instance = entry.getValue();
                    Map<String, PopulatorCategory> found = getPopulatorCategory(instance);
                    found.entrySet().stream().findFirst().ifPresent(e -> {
                        PopulatorCategory category = e.getValue();
                        if (category.autoSync()) {
                            beanName2InstanceWhichDesire2Sync.put(beanIdentity, instance);
                            initExecutorAndTimerTask();
                        }
                    });
                });
    }

    @Override
    public <T> Try<T> refresh(T instance) {
        return Try.of(() -> {
            getPopulatorCategoryCode(instance).stream()
                    .forEach(populatorCategoryCode ->
                            this.cache.refresh(populatorCategoryCode)
                    );
            return populate(instance).get();
        });
    }

    @Override
    public <T> Try<T> refreshClass(Class<T> clazz) {
        return populate(ctx.getBean(clazz));
    }

    private void initExecutorAndTimerTask() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    beanName2InstanceWhichDesire2Sync.entrySet().stream().forEach(entry -> {
                        refresh(entry.getValue());
                    });
                }
            }, syncIntervalInMinutes(), syncIntervalInMinutes(), TimeUnit.MINUTES);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("ExecutorAndTimerTask is ready.");
            }
        }
    }

    @Override
    public Try<Void> refreshAll() {
        this.cache.cleanUp();
        return loadAll();
    }

    @Override
    public Result<Map<String, Object>> manuallyReload(String action) {

        if ("all".equals(action)) {
            return manuallyRefreshAll();
        } else {
            return refreshOne(action);
        }

    }

    private Result refreshOne(String beanName) {
        Result result = new Result();

        getInstance(beanName).flatMap(this::refresh)
                .onSuccess(t -> {
                    String msg = "Success to refresh " + beanName + " @" + Calendar.getInstance().getTime() + ": " + JacksonUtils.objectToJson(result);
                    result.setData(t.toString());
                    result.setMessage(msg);
                    logger.debug(msg);
                })
                .onFailure(e -> {
                    String msg = "Fail to refresh " + beanName + " @" + Calendar.getInstance().getTime() + " due to: " + e.getMessage();
                    result.setMessage(msg);
                    result.setResult(Result.FAILURE);
                    logger.error("Fail to refresh " + beanName, e);
                });

        return result;
    }

    private Result manuallyRefreshAll() {
        Result result = new Result();
        refreshAll()
                .onSuccess(nil -> {
                    String msg = "Success to refresh all. @" + Calendar.getInstance().getTime();
                    result.setMessage(msg);
                    logger.debug(msg);
                }).
                onFailure(e -> {
                    String msg = "Fail to refresh all. @" + Calendar.getInstance().getTime();
                    result.setMessage(msg);
                    result.setResult(Result.FAILURE);
                    logger.error(msg, e);
                });
        return result;
    }

    private <T> Try<T> getInstance(String name) {
        return Try.of(() -> (T) ctx.getBean(name));
    }

    @Override
    public <T> Try<T> fetch(Class<T> clazz) {
        return Try.of(() -> ReflectionUtils.newInstance(clazz).get()).flatMap(this::populate);
    }

    @Override
    public <T> Try<T> populate(T instance) {
        return Try.of(() -> {

            List<String> lookupClassifyCodes = getPopulatorCategoryCode(instance);
            Map<String, Map<String, String>> code2Field2Target = getPopulatorItemCode2Field2Target(instance);

            for (String lookupClassifyCode : lookupClassifyCodes) {
                List<PopulatorResource> lookupItems = getPopulatorItems(lookupClassifyCode);

                if (CollectionUtils.isEmpty(lookupItems)) {
                    continue;
                }

                lookupItems.stream().forEach(itm ->
                        code2Field2Target.getOrDefault(itm.getItemCode(), Maps.newLinkedHashMap())
                                .entrySet().stream().forEach(entry -> populateItem(instance, lookupClassifyCode, itm, entry)
                                )
                );
            }

            populateDefaultIfUnset(instance);

            return instance;
        });
    }

    private <T> void populateDefaultIfUnset(T instance) {
        Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(byHasPopulatorItemAnnotatedAndHasDefaultValue()).collect(Collectors.toList()).stream()
                .filter(byValueIsUnset(instance))
                .forEach(field -> {
                    PopulatorItem ann = ReflectionUtils.getAnnotation(field, PopulatorItem.class);
                    ReflectionUtils.setFieldValueWithCast(instance, field.getName(), ann.defaultValue());
                    logger.info(instance.getClass().getSimpleName() + "." + field.getName() + " = " + ReflectionUtils.getFieldValueAsString(instance, field.getName()) +
                            " DEFAULT|-> " + ann.categoryCode() + " -> " + getPopulatorItemCode(field) + " -> " + ann.target());
                });
    }

    private <T> Predicate<Field> byValueIsUnset(T instance) {
        return field -> ReflectionUtils.getFieldValue(instance, field.getName()) == null;
    }

    private Predicate<Field> byHasPopulatorItemAnnotatedAndHasDefaultValue() {
        return field -> ReflectionUtils.hasAnnotation(field, PopulatorItem.class)
                && !Strings.isNullOrEmpty(ReflectionUtils.<PopulatorItem>getAnnotation(field, PopulatorItem.class).defaultValue());
    }

    private <T> void populateItem(T instance, String populatorCategoryCode, PopulatorResource itm, Map.Entry<String, String> entry) {
        String fieldName = entry.getKey();
        final String prefix = instance.getClass().getSimpleName() + "." + fieldName;

        Field targetField = ReflectionUtils.getDeclaredField(instance, fieldName);
        PopulatorItem annoItem = targetField.getAnnotation(PopulatorItem.class);

        if (annoItem != null && !Strings.isNullOrEmpty(annoItem.categoryCode())
                && !annoItem.categoryCode().equals(populatorCategoryCode)) {
            logger.warn(prefix + " skipped due to mismatched populatorCategoryCode: " + populatorCategoryCode + ", expected: " + annoItem.categoryCode());
            return;
        }

        String target = entry.getValue();
        String targetValue = ReflectionUtils.getFieldValueAsString(itm, target);

        ReflectionUtils.setFieldValueWithCast(instance, fieldName, targetValue);
        logger.info(prefix + " = " + ReflectionUtils.getFieldValueAsString(instance, fieldName) +
                " |-> " + populatorCategoryCode + " -> " + itm.getItemCode() + " -> " + target);
    }

    private List<PopulatorResource> getPopulatorItems(String populatorCategoryCode) throws ExecutionException {
        return cache.get(populatorCategoryCode);
    }

    private Map<String, Map<String, String>> getPopulatorItemCode2Field2Target(Object instance) {
        PopulatorCategory populatorCategory = getPopulatorCategory(instance).values().stream().findFirst().get();
        return Arrays.asList(instance.getClass().getDeclaredFields()).stream()
                .filter(byPopulatingScope(populatorCategory))
                .collect(Collectors.groupingBy(
                                this::getPopulatorItemCode,
                                Collectors.toMap(Field::getName, this::getTarget)
                        )
                );
    }

    private Predicate<Field> byPopulatingScope(PopulatorCategory populatorCategory) {
        return field -> ReflectionUtils.hasAnnotation(field, PopulatorItem.class) || !populatorCategory.populateAnnotatedFieldsOnly();
    }

    private String getTarget(Field field) {
        PopulatorItem populatorItem = field.getAnnotation(PopulatorItem.class);
        if (populatorItem == null)
            return PopulatorResource.DEFAULT_TARGET;

        return populatorItem.target();
    }

    private String getPopulatorItemCode(Field field) {
        String fname = field.getName();
        PopulatorItem populatorItem = field.getAnnotation(PopulatorItem.class);
        if (populatorItem == null)
            return fname;

        return !Strings.isNullOrEmpty(populatorItem.code()) ? populatorItem.code() : fname;
    }

    private Map<String, PopulatorCategory> getPopulatorCategory(Object instance) {
        Map<String, PopulatorCategory> className2PopulatorCategory = ReflectionUtils.getClassName2DeclaredAnnotation(instance, PopulatorCategory.class);

        if (className2PopulatorCategory.isEmpty())
            throw new RuntimeException("Annotation @PopulatorCategory of Class " + instance.getClass().getSimpleName() + " is required.");

        return className2PopulatorCategory;
    }

    private List<String> getPopulatorCategoryCode(Object instance) {

        Map<String, PopulatorCategory> found = getPopulatorCategory(instance);

        if (found.isEmpty())
            return Lists.newArrayList(instance.getClass().getSimpleName());

        String[] codes = found.values().stream().findFirst().get().codes();
        if (codes != null && codes.length > 0)
            return Lists.newArrayList(codes);

        return Lists.newArrayList(found.keySet().stream().findFirst().get());

    }

    private LoadingCache<String, List<PopulatorResource>> initLoadingCache() {
        return CacheBuilder.newBuilder()
                .refreshAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build(new PopulatorCacheLoader());
    }

    private class PopulatorCacheLoader extends CacheLoader<String, List<PopulatorResource>> {
        @Override
        public List<PopulatorResource> load(String categoryCode) {
            return loadItems(categoryCode);
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}