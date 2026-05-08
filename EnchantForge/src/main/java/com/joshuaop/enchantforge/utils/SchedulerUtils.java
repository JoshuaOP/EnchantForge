package com.joshuaop.enchantforge.utils;

import com.joshuaop.enchantforge.EnchantForge;
import org.bukkit.entity.Entity;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility wrappers for the modern Paper async and entity schedulers.
 * All task execution goes through Paper's Folia-compatible API.
 */
public final class SchedulerUtils {

    private SchedulerUtils() {}

    /** Run a task on the main thread at the next tick. */
    public static void sync(Runnable task) {
        EnchantForge plugin = EnchantForge.getInstance();
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    /** Run a task on the main thread after a delay in ticks. */
    public static void syncDelayed(Runnable task, long delayTicks) {
        EnchantForge plugin = EnchantForge.getInstance();
        plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /** Run a task asynchronously immediately. */
    public static void async(Runnable task) {
        EnchantForge plugin = EnchantForge.getInstance();
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> task.run());
    }

    /** Run a task asynchronously after a delay in milliseconds. */
    public static void asyncDelayed(Runnable task, long delayMs) {
        EnchantForge plugin = EnchantForge.getInstance();
        plugin.getServer().getAsyncScheduler().runDelayed(
                plugin, t -> task.run(), delayMs, TimeUnit.MILLISECONDS);
    }

    /** Run a task on an entity's thread (Folia-safe). */
    public static void entity(Entity entity, Runnable task) {
        EnchantForge plugin = EnchantForge.getInstance();
        entity.getScheduler().run(plugin, t -> task.run(), null);
    }

    /** Run a task on an entity's thread after a delay in ticks. */
    public static void entityDelayed(Entity entity, Runnable task, long delayTicks) {
        EnchantForge plugin = EnchantForge.getInstance();
        entity.getScheduler().runDelayed(plugin, t -> task.run(), null, delayTicks);
    }
}
